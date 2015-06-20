package com.htvu.instamua.rest.api.services

import java.util.UUID

import akka.actor.{Actor, ActorSystem, Props, Status}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import awscala._
import awscala.s3._
import com.amazonaws.services.s3.model.ObjectMetadata
import com.htvu.instamua.rest.Configs
import com.htvu.instamua.rest.api.JsonFormats
import com.htvu.instamua.rest.dao.{Image, Media}
import spray.http.{BodyPart, MultipartFormData}
import spray.routing.Directives

import scala.concurrent.{ExecutionContext, Future, Promise}

class MediaService()(implicit system: ActorSystem) extends Directives {
  val mediaCtlr = system.actorOf(Props(new MediaController()))
  import scala.concurrent.duration._
  implicit val timeout = Timeout(5.seconds)

  val routes = pathPrefix(Segment / "media") { endPoint => // endPoint = 'listings' or 'users'
    path("upload") {
      post {
        entity(as[MultipartFormData]) { formData =>
          ctx => {
            implicit def marshaller = JsonFormats.json4sMarshaller
            ctx complete (mediaCtlr ? MediaController.FileUpload(endPoint, formData)).mapTo[List[Media]]
          }
        }
      }
    }
  }

  implicit def executionContext: ExecutionContext = system.dispatcher
}

object MediaController {
  def props: Props = Props(new MediaController())
  case class FileUpload(endPoint: String, formData: MultipartFormData)
}

class MediaController() extends Actor {
  val promises =  scala.collection.mutable.Map[String, Promise[List[Media]]]()
  val done  = scala.collection.mutable.Map[String, List[Media]]()
  val expectedNumParts = scala.collection.mutable.Map[String, Int]()

  import MediaController._
  import MediaUploader._

  implicit def executionContext = context.dispatcher

  def receive: Receive = {
    case FileUpload(endPoint, data) =>
      val p = Promise[List[Media]]()
      val hash = UUID.randomUUID().toString
      data.fields.foreach(part => context.actorOf(MediaUploader.props(endPoint, hash, part)))
      promises += hash -> p
      done += hash -> Nil
      expectedNumParts += hash -> data.fields.size
      p.future pipeTo sender
    case UploadSuccess(h, x) =>
      val p = promises(h)
      val xs = x::done(h)
      done += h -> xs
      if (xs.size == expectedNumParts(h)) {
        println("successful")
        p.success(xs)
        clear(h)
      }
    case UploadFailed(h, e) =>
      val p = promises(h)
      p.failure(e)
      clear(h)
  }

  private def clear(h: String) = {
    promises -= h
    done -= h
    expectedNumParts -= h
  }
}

object MediaUploader {
  def props(endPoint: String, uploadId: String, data: BodyPart): Props = Props(new MediaUploader(endPoint, uploadId, data))

  case class UploadSuccess(uploadId: String, x: Media)
  case class UploadFailed(uploadId: String, e: Throwable)
}

class MediaUploader(endPoint: String, uploadId: String, data: BodyPart) extends Actor with S3Service with MediaResize {
  import MediaUploader._

  val resizeUpload = for {
    thumbnail <- resize(data.entity.data.toByteArray)
    image <- uploadPhoto(endPoint, "thumbnail")(data.filename.get, thumbnail)
  } yield image

  val uploadStandard = uploadPhoto(endPoint, "standard")(data.filename.get, data.entity.data.toByteArray)

  // combine 2 futures and return a future of uploaded Media
  val futureMedia = Future.sequence(List(uploadStandard, resizeUpload)) map (l =>
    Media(standard = Some(Image(l.head, 160, 160)), thumbnail = Some(Image(l(1), 80, 80))))

  futureMedia pipeTo self

  def receive: Actor.Receive = {
    case x: Media =>
      context.parent ! UploadSuccess(uploadId, x)
      context.stop(self)
    case Status.Failure(e) =>
      context.parent ! UploadFailed(uploadId, e)
  }

  implicit def executionContext: ExecutionContext = context.dispatcher
}

trait MediaResize {
  def resize(image: Array[Byte]): Future[Array[Byte]] = Future.successful(image)
}

trait S3Service {
  implicit val region = Region.AP_SOUTHEAST_1
  implicit val s3 = S3(Configs.config.getString("aws.access-key-id"), Configs.config.getString("aws.secret-access-key"))

  private val baseBucket = "instamua/media/photos"
  private val s3BaseUrl = s"https://s3-${Region.AP_SOUTHEAST_1.getName}.amazonaws.com"

  implicit def executionContext: ExecutionContext

  def upload(bucket: String)(filename: String, data: Array[Byte]): Future[String] = {
    Future {
      val meta = new ObjectMetadata()
      meta.setContentLength(data.length)
      s3.put(
        Bucket(s"$baseBucket/$bucket"),
        filename,
        data,
        meta
      )
      s"$s3BaseUrl/$baseBucket/$bucket/$filename"
    }
  }

  def uploadPhoto(endPoint: String, resolution: String): (String, Array[Byte]) => Future[String] = upload(s"$endPoint/$resolution")
}
