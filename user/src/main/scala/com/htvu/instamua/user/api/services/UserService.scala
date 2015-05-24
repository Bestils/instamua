package com.htvu.instamua.user.api.services

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.htvu.instamua.user.dao.{UserPrivateInfo, UserRegistrationInfo, User, UserDAO}
import UserDAO.UserSearchResult
import com.htvu.instamua.user.dao.UserPrivateInfo
import com.htvu.instamua.user.api.JsonFormats
import com.htvu.instamua.user.core.UserActor
import spray.http.MediaTypes.`application/json`
import spray.routing.Directives

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

class UserService()(implicit system: ActorSystem) extends Directives with JsonFormats {

  val userActor = system.actorOf(UserActor.props())

  import UserActor._

  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)

  val routes = pathPrefix("users") {
    pathEnd {
      post {
        handleWith { userInfo: UserRegistrationInfo =>
          (userActor ? RegisterNewUser(userInfo)).mapTo[Option[User]]
        }
      }
    } ~
    pathPrefix(IntNumber) { userId =>
      pathEnd {
        get {
          respondWithMediaType(`application/json`)(ctx =>
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
            handleWith { userInfo: User =>
              (userActor ? UpdateUserInfo(userInfo)).mapTo[Try[Int]]
            }
          }
      } ~
      path("private-info") {
        pathEnd {
          get {
            respondWithMediaType(`application/json`)(
              _ complete (userActor ? GetUserPrivateInfo(userId)).mapTo[Option[UserPrivateInfo]]
            )
          } ~
            put {
              handleWith { userPrivateInfo: UserPrivateInfo =>
                (userActor ? UpdateUserPrivateInfo(userPrivateInfo)).mapTo[Try[Int]]
              }
            }
        }
      } ~
      path("listings" / "recent") {
        get {
          respondWithMediaType(`application/json`) {
            _ complete """{"recent listings": "OK"}"""
          }
        }
      } ~
      path("followers") {
        get {
          respondWithMediaType(`application/json`) {
            _ complete (userActor ? GetFollowers(userId))
          }
        }
      } ~
      path("followings") {
        get {
          respondWithMediaType(`application/json`) {
            _ complete (userActor ? GetFollowings(userId))
          }
        }
      } ~
      path("relationship") {
        get {
          respondWithMediaType(`application/json`) {
            _ complete """{"relationship": "OK"}"""
          }
        }
      } ~
      path("relationship") {
        post {
          respondWithMediaType(`application/json`) {
            _ complete """{"new relationship": "OK"}"""
          }
        }
      }
    } ~
    path("search") {
      get {
        parameters("q") { query =>
          respondWithMediaType(`application/json`) {
            _ complete (userActor ? SearchUser(query)).mapTo[Seq[UserSearchResult]]
          }
        }
      }
    } ~
    path("self" / "feed") {
      get {
        respondWithMediaType(`application/json`) {
          _ complete """{"feeds": "OK"}"""
        }
      }
    }
  }
}
