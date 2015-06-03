package com.htvu.instamua.user.api.services

import akka.actor.ActorSystem
import com.htvu.instamua.user.api.JsonFormats
import spray.routing.Directives

class ListingService()(implicit system: ActorSystem) extends Directives with JsonFormats {

  val routes = pathPrefix("listings") {
    pathEnd {
      post {
        ???
      }
    } ~
    pathPrefix(IntNumber) { listingId =>
      pathEnd {
        get {
          ???
        } ~
        put {
          ???
        }
      } ~
      pathPrefix("comments") {
        pathEnd {
          get {
            ???
          } ~
          post {
            ???
          }
        } ~
        path(IntNumber) { commentId =>
          put {
            ???
          } ~
          delete {
            ???
          }
        }
      } ~
      path("likes") {
        get {
          ???
        } ~
        post {
          ???
        } ~
        delete {
          ???
        }
      }
    } ~
    path("search") {
      get {
        parameter("q") { query =>
          ???
        }
      }
    }
  }

}
