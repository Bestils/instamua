package com.htvu.instamua.rest.api.services

import akka.actor.{ActorRefFactory, ActorSystem}
import akka.util.Timeout
import com.htvu.instamua.rest.dao._
import shapeless.{HNil, ::}
import spray.http.MediaTypes._

import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.json4s.native.JsonMethods._
import spray.http.StatusCodes._

import spray.routing._
import com.htvu.instamua.rest.session._

import spray.routing._

import spray.routing.{HttpService, RequestContext, AuthenticationFailedRejection}
import concurrent.Future
import spray.routing.authentication.Authentication
import com.htvu.instamua.rest.dao._
import scala.util.{Failure, Success}
import scala.concurrent._
import scala.concurrent.duration._
import com.htvu.instamua.rest.SettingsProvider
import akka.pattern.ask
import akka.io.IO

import spray.can.Http
import spray.http._
import HttpMethods._
import spray.client.pipelining._
import com.htvu.instamua.rest.api._
import com.typesafe.scalalogging.LazyLogging
import spray.httpx.unmarshalling.{Unmarshaller, FromResponseUnmarshaller}
import java.net.URLEncoder;

object TokenType extends Enumeration(1) {
  type TokenType = Value
  val AuthorizationCode, AccessToken, RefreshToken = Value
}

//this is incharge of static files and proxy request into nodejs server and other third party
class StaticService()(implicit system: ActorSystem) extends Directives with SessionCookieAuthenticatorProvider 
with SessionCookieAuthorizationProvider with SettingsProvider with LazyLogging{
  implicit def actorRefFactory: ActorRefFactory = system
  implicit val ec = system.dispatcher
  import RestJsonFormatProtocol._
  import TokenType._
  
  //main route
  val routes = {
    pathSingleSlash {
      get {
        respondWithMediaType(`text/html`)(
          _ complete (IO(Http) ? HttpRequest(GET, Uri(settings.NodeJs.Url))).mapTo[HttpResponse]
        )
      }
    } ~
    path("login") {
      cookieSession() { (id, _) =>
        parameters('code.as[String]) { (code) =>
          var pipeline = sendReceive ~> unmarshal[RestResponse]

          var authorizeVerification = pipeline(
            Get(settings.SSO.Url + "/api/oauth/verifyToken?code=" + code + "&codeType=" + AuthorizationCode.id.toString())
          )
          
          respondWithMediaType(`text/html`)( ctx =>
            authorizeVerification.onComplete {
              case Success(response: RestResponse) => {
                if (response.status.get == "success") {
                  //login successfully -- we get the user id from the sso server and then query from the database
                  var ssoUserId: Int = (response.data.get \ "id").extract[Int]

                  UserDAO.getUserInfoSSO(ssoUserId).mapTo[Option[User]] onComplete {
                    case Success(userOption) => userOption match {
                      case None => {
                        val redirectUrl = settings.SSO.Url + "/loginError?title=" +
                          URLEncoder.encode("Login Error", "UTF-8") + "&description=" + 
                          URLEncoder.encode("User does not exist", "UTF-8");

                        ctx.redirect(redirectUrl, StatusCodes.PermanentRedirect)
                      }
                      case Some(user) => {
                        ctx complete user
                      }
                    }
                  }
                }
                else
                {
                  respondWithStatus(InternalServerError) {
                    getFromResource("statics/errors/500.html")
                  }
                }
              }
              case Failure(e) => {
                respondWithStatus(InternalServerError) {
                  getFromResource("statics/errors/500.html")
                }
              }
            }
          )
        }
      }
    } ~
    path("register") {
      cookieSession() { (id, _) =>
        parameters('code.as[String]) { (code) =>
          var pipeline = sendReceive ~> unmarshal[RestResponse]

          var authorizeVerification = pipeline(
            Get(settings.SSO.Url + "/api/oauth/verifyToken?code=" + code + "&codeType=" + AuthorizationCode.id.toString())
          )

          respondWithMediaType(`text/html`)( ctx =>
            authorizeVerification.onComplete {
              case Success(response: RestResponse) => {
                if (response.status.get == "success") {
                  //we create a new user now
                  val userData:JValue = response.data.get;
                  var userName:String = (userData \ "username").extract[String]
                  var fullname:String = (userData \ "fullname").extract[String]
                  var email:String = (userData \ "email").extract[String]
                  var ssoId:Int = (userData \ "id").extract[Int]
                  
                  var userRegistrationInfo:UserRegistrationInfo = UserRegistrationInfo(userName, "", email, "", fullname, ssoId)
                  ctx complete UserDAO.createNewUser(userRegistrationInfo)
                }
                else
                {
                  respondWithStatus(InternalServerError) {
                    getFromResource("statics/errors/500.html")
                  }
                }
              }
              case Failure(e) => {
                respondWithStatus(InternalServerError) {
                  getFromResource("statics/errors/500.html")
                }
              }
            }
          )
        }
      }
    }
  }
}
