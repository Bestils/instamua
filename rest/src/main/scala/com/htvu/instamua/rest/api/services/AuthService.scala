package com.htvu.instamua.rest.api.services

import akka.actor.{ActorRefFactory, ActorSystem}
import akka.util.Timeout
import com.htvu.instamua.rest.dao._
import spray.http.MediaTypes._
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}
import spray.routing._
import com.htvu.instamua.rest.session._

import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol._
import com.htvu.instamua.rest.session.WithStatefulManagerMagnet

import scala.concurrent.duration._

class AuthService()(implicit system: ActorSystem) extends Directives with StatefulSessionManagerDirectives[Int]{
  implicit val ec = system.dispatcher
  implicit val timeout = new Timeout(Duration(2, SECONDS))
  implicit val manager = new RedisSessionManager[Int](ConfigFactory.load())
  
  val routes = pathPrefix("auth") {
    cookieSession() { (id, map) =>
      pathPrefix("login"){
        pathEnd {
          get {
            complete {
              <h1>Say hello to spray</h1>
            }
          }
        }
      }
    }
  }
}
