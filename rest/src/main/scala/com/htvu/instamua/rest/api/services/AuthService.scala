package com.htvu.instamua.rest.api.services

import akka.actor.ActorSystem
import akka.util.Timeout
import com.htvu.instamua.rest.dao._
import spray.http.MediaTypes._
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}
import spray.routing._
import spray.routing.se

class AuthService()(implicit system: ActorSystem) extends Directives{
  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)
  implicit val formats = Serialization.formats(NoTypeHints)
  
  val routes = pathPrefix("auth") {
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
