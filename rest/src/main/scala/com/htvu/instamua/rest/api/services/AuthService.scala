package com.htvu.instamua.rest.api.services

import akka.actor.ActorSystem
import akka.util.Timeout
import com.htvu.instamua.rest.dao._
import akka.pattern.ask
import spray.http.StatusCode
import spray.routing._
import spray.http.MediaTypes._
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}
import spray.routing._
import shapeless.HNil
import shapeless._
import Directives._
import scala.concurrent._;
import spray.routing.authentication._

import spray.routing.authentication.Authentication
import spray.routing.authentication.ContextAuthenticator
import scala.concurrent.Future
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext.Implicits.global
import spray.routing.AuthenticationFailedRejection


class AuthService()(implicit system: ActorSystem) extends Directives{
  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)
  implicit val formats = Serialization.formats(NoTypeHints)
  
  val routes = pathPrefix("auth") {
    pathPrefix("register"){
      pathEnd {
        get {
          respondWithMediaType(`text/html`) {
            complete {
              <h1>Say hello to spray</h1>
            }
          }
        }
      }
    } ~
    pathPrefix("login"){
      pathEnd {
        post {
          complete {
            <h1>Say hello to spray</h1>
          }
        } ~
          get {
            respondWithMediaType(`application/json`) {
              complete {
                var newUser:User = User(1, "nguy0066", Some("Nguyen Xuan Tuong"), Some(""),Some(""),Some(""),Some(""))
                write(newUser)
              }
            }
          }
      }
    } ~
    pathPrefix("token"){
      pathEnd {
        get {
          respondWithMediaType(`application/json`) {
            complete {
              """ [
                    {"name": "banana", "price": "0.79"},
                     {"name": "apple", "price": "1.89"},
                    {"name": "raspberry", "price": "12.50"}
                  ]"""
            }
          }
        }
      }
    }
  }
}
