package com.htvu.instamua.rest.api.services

import akka.actor.ActorSystem
import awscala._
import awscala.s3._
import com.amazonaws.services.s3.model.ObjectMetadata
import com.htvu.instamua.rest.Configs
import com.htvu.instamua.rest.api.JsonFormats
import com.htvu.instamua.rest.dao.{Image, Media}
import spray.http.MultipartFormData
import spray.routing.Directives

import scala.concurrent.{ExecutionContext, Future}

class MediaService()(implicit system: ActorSystem) extends Directives  with S3Service {
  val routes = pathPrefix("listing" / "media") {
    path("upload") {
      post {
        entity(as[MultipartFormData]) { formData =>
          ctx => {
            implicit def marshaller = JsonFormats.json4sMarshaller
            ctx.complete(uploadListingPhotos(formData))
          }
        }
      }
    }
  }

  implicit def executionContext: ExecutionContext = system.dispatcher
}

trait S3Service {
  private val IMAGE_STANDARD_WIDTH = 320.toShort
  private val IMAGE_STANDARD_HEIGHT = 320.toShort

  implicit val region = Region.AP_SOUTHEAST_1
  implicit val s3 = S3(Configs.config.getString("aws.access-key-id"), Configs.config.getString("aws.secret-access-key"))

  private val baseBucket = "instamua/media/photos"
  private val s3BaseUrl = s"https://s3-${Region.AP_SOUTHEAST_1.getName}.amazonaws.com"

  implicit def executionContext: ExecutionContext

  def upload(formData: MultipartFormData, bucket: String): Future[Seq[Media]] = {
    Future.sequence(formData.fields.map(part => Future {
      val meta = new ObjectMetadata()
      meta.setContentLength(part.entity.data.length)
      s3.put(
        Bucket(s"$baseBucket/$bucket"),
        part.filename.get,
        part.entity.data.toByteArray,
        meta
      )
      Media(standard = Some(Image(s"$s3BaseUrl/$baseBucket/$bucket/${part.filename.get}", IMAGE_STANDARD_WIDTH, IMAGE_STANDARD_HEIGHT)))
    }))
  }

  def uploadListingPhotos(formData: MultipartFormData): Future[Seq[Media]] =
    upload(formData, "listings/standard")

  def uploadUserProfilePicture(formData: MultipartFormData): Future[Media] =
    upload(formData, "users/standard") map (_.head)
}
