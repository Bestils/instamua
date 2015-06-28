package com.htvu.instamua.rest.api.services

import akka.actor.{ActorRefFactory, ActorSystem}
import akka.util.Timeout
import com.htvu.instamua.rest.dao._
import com.typesafe.scalalogging.LazyLogging
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
import HttpMethods._

//similar to static files but to proxy other RESTFUL request into nodejs server and other third party
//TODO: need to research a way for more efficient proxy request with multiple concurrent requests
//NOTE: some of the API overhere just for testing only; theoritically; only special API should be forwarded to Node (Very few); 
//the rest should be handled properly by 'main' spray server
class ThirdPartyService()(implicit system: ActorSystem) extends Directives with SessionCookieAuthenticatorProvider 
with SessionCookieAuthorizationProvider with LazyLogging{
  implicit def actorRefFactory: ActorRefFactory = system
  implicit val ec = system.dispatcher

  val routes = {
    path("home" / "listings") { 
      parameters('limit.as[Int] ? 100, 'offset.as[Int] ? 0) { (limit, offset) =>
        val forwardUrl:String = settings.NodeJs.Url + "/api/v1/home/listings?limit=" + limit + "&offset=" + offset;
        respondWithMediaType(`application/json`)(ctx =>
          ctx complete (IO(Http) ? HttpRequest(GET, Uri(forwardUrl))).mapTo[HttpResponse]
        )
      }
    }
  }
}
