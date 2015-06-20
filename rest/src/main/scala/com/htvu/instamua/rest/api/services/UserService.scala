package com.htvu.instamua.rest.api.services

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.htvu.instamua.rest.api.JsonFormats
import com.htvu.instamua.rest.dao.Relationship.Relationship
import com.htvu.instamua.rest.dao._
import com.htvu.instamua.rest.util.ActorExecutionContextProvider
import spray.http.MediaTypes.`application/json`
import spray.routing.Directives

import scala.util.{Success, Try}

class UserService()(implicit system: ActorSystem) extends Directives with JsonFormats {
  import UserActor._
  val userActor = system.actorOf(UserActor.props(), "user-actor")
  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)

  implicit val ec = system.dispatcher

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

object UserActor {
  case class GetUserInfo(userId: Int)
  case class RegisterNewUser(userInfo: UserRegistrationInfo)
  case class GetUserPrivateInfo(userId: Int)
  case class UpdateUserInfo(newInfo: User)
  case class UpdateUserPrivateInfo(newPrivateInfo: UserPrivateInfo)
  case class SearchUser(query: String)
  case class GetFollowers(userId: Int)
  case class GetFollowings(userId: Int)
  case class GetRelationship(userId: Int, otherId: Int)
  case class PostRelationship(userId: Int, otherId: Int)

  def props(): Props = Props(new UserActor())
}

//retrieve implicit execution context by mixin ActorExecutionContextProvider
class UserActor extends Actor with ActorExecutionContextProvider {
  import UserActor._

  def receive: Receive = {
    case RegisterNewUser(userInfo: UserRegistrationInfo) =>
      UserDAO.createNewUser(userInfo) pipeTo sender
    case GetUserInfo(userId) =>
      UserDAO.getUserInfo(userId) pipeTo sender
    case GetUserPrivateInfo(userId) =>
      UserDAO.getUserPrivateInfo(userId) pipeTo sender
    case UpdateUserInfo(newUserInfo) =>
      UserDAO.updateUserInfo(newUserInfo) pipeTo sender
    case UpdateUserPrivateInfo(newUserPrivateInfo: UserPrivateInfo) =>
      UserDAO.updateUserPrivateInfo(newUserPrivateInfo) pipeTo sender
    case SearchUser(query) =>
      UserDAO.searchUser(query) pipeTo sender
    case GetFollowers(userId) =>
      UserDAO.getFollowers(userId) pipeTo sender
    case GetFollowings(userId) =>
      UserDAO.getFollowings(userId) pipeTo sender
    case GetRelationship(userId, otherId) =>
      UserDAO.getRelationship(userId, otherId) pipeTo sender
    case PostRelationship(userId, otherId) =>
      UserDAO.getRelationship(userId, otherId) flatMap(curRel =>
        UserDAO.postRelationship(userId, otherId, curRel)
        ) pipeTo sender
  }
}
