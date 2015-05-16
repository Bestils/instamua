package com.htvu.cclone.user.api.services

import spray.http.MediaTypes.`application/json`
import spray.routing.Directives

class RelationshipService extends Directives {
  val routes = pathPrefix("users" / IntNumber) { userId =>
    path("follows") {
      get {
        respondWithMediaType(`application/json`) { _ complete """{"follow": "OK"}"""}
      }
    } ~
    path("followed-by") {
      get {
        respondWithMediaType(`application/json`) { _ complete """{"follow-by": "OK"}"""}
      }
    } ~
    path("requested-by") {
      get {
        respondWithMediaType(`application/json`) { _ complete """{"requested-by": "OK"}"""}
      }
    } ~
    path("relationship") {
      get {
        respondWithMediaType(`application/json`) { _ complete """{"relationship": "OK"}"""}
      }
    } ~
    path("relationship") {
      post {
        respondWithMediaType(`application/json`) { _ complete """{"new relationship": "OK"}"""}
      }
    }

  }
}
