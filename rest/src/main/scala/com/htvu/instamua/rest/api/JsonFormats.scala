package com.htvu.instamua.rest.api

import com.htvu.instamua.rest.session.SessionData
import com.redis.serialization.Format
import org.json4s.NoTypeHints
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import spray.httpx.Json4sSupport

trait JsonFormats extends Json4sSupport {
  implicit def json4sFormats = org.json4s.DefaultFormats
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


