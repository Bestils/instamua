package com.htvu.instamua.rest.api.services

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.htvu.instamua.rest.api.JsonFormats
import com.htvu.instamua.rest.core.UserActor
import com.htvu.instamua.rest.dao.Relationship.Relationship
import com.htvu.instamua.rest.dao._
import spray.http.MediaTypes.`application/json`
import spray.routing.Directives
import akka.event.slf4j.SLF4JLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

class UserService()(implicit system: ActorSystem) extends Directives with JsonFormats {

  val userActor = system.actorOf(UserActor.props(), "user-actor")

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
                case Some(user) => {
                  ctx complete user
                }
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
            _ complete (userActor ? GetFollowers(userId)).mapTo[Seq[FollowerListResult]]
          }
        }
      } ~
      path("followings") {
        get {
          respondWithMediaType(`application/json`) {
            _ complete (userActor ? GetFollowings(userId)).mapTo[Seq[FollowerListResult]]
          }
        }
      } ~
      path("relationship" / IntNumber) { otherId =>
        get {
          respondWithMediaType(`application/json`) {
            _ complete (userActor ? GetRelationship(userId, otherId)).mapTo[Relationship]
          }
        } ~
        post {
          respondWithMediaType(`application/json`) {
            _ complete (userActor ? PostRelationship(userId, otherId)).mapTo[Int]
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
