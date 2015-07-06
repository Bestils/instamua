package com.htvu.instamua.rest.api.services

import akka.actor.{ActorRefFactory, ActorSystem}
import akka.util.Timeout
import com.htvu.instamua.rest.dao._
import shapeless.{HNil, ::}
import spray.http.MediaTypes._
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}
import org.json4s.native.JsonMethods._
import spray.routing._
import com.htvu.instamua.rest.session._

import spray.routing._

import spray.routing.{HttpService, RequestContext, AuthenticationFailedRejection}
import concurrent.Future
import spray.routing.authentication.Authentication
import com.htvu.instamua.rest.dao._
import scala.concurrent._
import scala.concurrent.duration._

import akka.pattern.ask
import akka.io.IO

import spray.can.Http
import spray.http._
import spray.client.pipelining._
import HttpMethods._

//StatefulSessionManagerDirectives[SessionData] with JsonSessionFormat
class AuthService()(implicit system: ActorSystem) extends Directives with SessionCookieAuthenticatorProvider with SessionCookieAuthorizationProvider{
  implicit def actorRefFactory: ActorRefFactory = system
  implicit val ec = system.dispatcher
  case class RestResponse[T](status: Option[String], errors: Option[String], data: Option[T])
  val routes = {
    cookieSession() { (id, _) =>
      path("oauth" / "login") {
        parameters('code.as[String]) { (code) =>
          //TODO: forward to go server to verify the authorize code
          respondWithMediaType(`text/html`)(
            _ complete "login success"
          )
        }
      } ~
      path("oauth" / "test") {
        _ complete ""
      }
    }
  }
}
