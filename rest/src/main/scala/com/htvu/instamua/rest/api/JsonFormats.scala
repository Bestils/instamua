package com.htvu.instamua.rest.api

import com.htvu.instamua.rest.session.SessionData
import com.redis.serialization.Format
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import reactivemongo.bson.BSONObjectID
import spray.httpx.Json4sSupport
import scala.util.Success

trait JsonFormats extends Json4sSupport {
  implicit def json4sFormats = org.json4s.DefaultFormats + BSONObjectIDSerializer

  val BSONObjectIDSerializer = new CustomSerializer[BSONObjectID](format => (
    {
      case JString(s) => BSONObjectID.parse(s) match {
        case Success(id) => id
        case _ => throw new Exception("invalid BSONObjectID format")
      }
      case _ => throw new Exception("invalid BSONObjectID format")
    },
    {
      case id: BSONObjectID => JString(id.stringify)
    }
  ))
}

object JsonFormats extends Json4sSupport {
  implicit def json4sFormats = org.json4s.DefaultFormats
}
//special class for redis session serialization
trait JsonSessionFormat {
  implicit val formats = Serialization.formats(NoTypeHints)

  //TODO: check if there is better way to do that
  implicit val sessionDataFormat = new Format[SessionData] {
    def read(str: String) = {
      val emptySessionData = SessionData(None, None)
      str match {
        case "" => emptySessionData
        case _ => parse(str).extract[SessionData]
      }
    }

    def write(sessionObj: SessionData) = {
      org.json4s.native.Serialization.write(sessionObj)
    }
  }
}

object JsonSessionFormat extends JsonSessionFormat

object RestJsonFormatProtocol extends Json4sSupport{
  override implicit def json4sFormats: Formats = DefaultFormats
  case class RestResponse(status: Option[String], errors: Option[String], data: Option[JValue])
}
