package com.htvu.cclone.user.api.services

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.htvu.cclone.user.api.DefaultJsonFormats
import com.htvu.cclone.user.core.UserInfoActor
import com.htvu.cclone.user.core.UserInfoActor.{UpdateUserPrivateInfo, UpdateUserInfo, GetUserInfo}
import com.htvu.cclone.user.dao.{UserPrivateInfo, User}
import spray.http.MediaTypes.`application/json`
import spray.routing.Directives
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Try, Success}

class UserInfoService()(implicit system: ActorSystem) extends Directives with DefaultJsonFormats {

  val userActor = system.actorOf(UserInfoActor.props())

  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)
  implicit val userFormat = jsonFormat7(User)

  val routes = pathPrefix("users") {
    pathPrefix(IntNumber) { userId =>
      pathEnd {
        get {
          respondWithMediaType(`application/json`) ( ctx =>
            (userActor ? GetUserInfo(userId)).mapTo[Option[User]] onComplete {
              case Success(userOption) => userOption match {
                case None => ctx complete """{"error": "user not found"}"""
                case Some(user) => ctx complete user
              }
              case _ => ctx complete """{"error": "Some thing is wrong"}"""
            }
          )
        } ~
        put {
          handleWith{ userInfo: User =>
            (userActor ? UpdateUserInfo(userInfo)).mapTo[Try[Int]]
          }
        }
      } ~
      path("private-info") {
        put {
          handleWith{ userPrivateInfo: UserPrivateInfo =>
            (userActor ? UpdateUserPrivateInfo(userPrivateInfo)).mapTo[Try[Int]]
          }
        }
      }
      path("listings" / "recent") {
        get {
          respondWithMediaType(`application/json`) { _ complete """{"recent listings": "OK"}""" }
        }
      }
    } ~
    path("search") {
      get {
        respondWithMediaType(`application/json`) { _ complete """{"user search": "OK"}""" }
      }
    } ~
    path("self" / "feed") {
      get {
        respondWithMediaType(`application/json`) { _ complete """{"feeds": "OK"}""" }
      }
    }
  }
}
