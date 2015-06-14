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


//StatefulSessionManagerDirectives[SessionData] with JsonSessionFormat
class AuthService()(implicit system: ActorSystem) extends Directives with SessionCookieAuthenticatorProvider{
  implicit def actorRefFactory: ActorRefFactory = system
  implicit val ec = system.dispatcher
  
  val routes = pathPrefix("auth") {
    cookieSession() { (id, map) =>
      pathPrefix("login"){
        pathEnd {
          get {
            val sessionData = SessionData(Some(User(1, "sa", None, None, None, None, None, None)), None)
            updateSession(id, sessionData) {
              session(id) { sessionObj =>
                println(sessionObj)
                complete {
                  <h1>Say hello to spray</h1>
                }
              }
            }
          }
        }
      }
    } ~
    path("restricted"){
      authenticate(SessionCookieAuthenticator) { session ⇒
        pathEnd {
          complete {
            <h1>Restricted Zone</h1>
          }
        }
      }
    }
  }
}
