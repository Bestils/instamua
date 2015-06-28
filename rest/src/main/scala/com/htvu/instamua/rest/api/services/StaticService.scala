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
import com.htvu.instamua.rest.SettingsProvider
import akka.pattern.ask
import akka.io.IO

import spray.can.Http
import spray.http._
import HttpMethods._
import com.typesafe.scalalogging.LazyLogging

//this is incharge of static files and proxy request into nodejs server and other third party
//TODO: need to research a way for more efficient proxy request with multiple concurrent requests (with ease) -- this one is more manual
class StaticService()(implicit system: ActorSystem) extends Directives with SessionCookieAuthenticatorProvider 
with SessionCookieAuthorizationProvider with SettingsProvider with LazyLogging{
  implicit def actorRefFactory: ActorRefFactory = system
  implicit val ec = system.dispatcher

  //main route
  val routes = {
    pathSingleSlash {
      get {
        respondWithMediaType(`text/html`)(
          _ complete (IO(Http) ? HttpRequest(GET, Uri(settings.NodeJs.Url))).mapTo[HttpResponse]
        )
      }
    }
  }
}
