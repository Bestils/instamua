package com.htvu.instamua.rest.api.services

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.htvu.instamua.rest.api.JsonFormats
import com.htvu.instamua.rest.dao.Relationship.Relationship
import com.htvu.instamua.rest.dao._
import com.htvu.instamua.rest.util.ActorExecutionContextProvider
import spray.routing.Directives

import scala.util.Try

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
          (userActor ? RegisterNewUser(userInfo)).toResponse[Int]
        }
      }
    } ~
    pathPrefix(IntNumber) { userId =>
      pathEnd {
        get {
            _ complete (userActor ? GetUserInfo(userId)).toResponse[Option[User]]
        } ~
          put {
            handleWith { userInfo: User =>
              (userActor ? UpdateUserInfo(userInfo)).toResponse[Try[Int]]
            }
          }
      } ~
      path("private-info") {
        pathEnd {
          get {
            _ complete (userActor ? GetUserPrivateInfo(userId)).toResponse[Option[UserPrivateInfo]]
          } ~
            put {
              handleWith { userPrivateInfo: UserPrivateInfo =>
                (userActor ? UpdateUserPrivateInfo(userPrivateInfo)).toResponse[Try[Int]]
              }
            }
        }
      } ~
      path("listings" / "recent") {
        get {
          _ complete """{"recent listings": "OK"}"""
        }
      } ~
      path("followers") {
        get {
          parameter('page.as[Int] ? 0) { page =>
            _ complete (userActor ? GetFollowers(userId, page)).toResponse[Seq[FollowerListResult]]
          }
        }
      } ~
      path("followings") {
        get {
          parameter('page.as[Int] ? 0) { page =>
            _ complete (userActor ? GetFollowings(userId, page)).toResponse[Seq[FollowerListResult]]
          }
        }
      } ~
      path("relationship" / IntNumber) { otherId =>
        get {
          _ complete (userActor ? GetRelationship(userId, otherId)).toResponse[Relationship]
        } ~
        post {
            _ complete (userActor ? PostRelationship(userId, otherId)).toResponse[Int]
        }
      }
    } ~
    path("search") {
      get {
        parameters('q, 'page.as[Int] ? 0  ) { (query, page) =>
          _ complete (userActor ? SearchUser(query, page)).toResponse[Seq[UserSearchResult]]
        }
      }
    } ~
    path("self" / "feed") {
      get {
        _ complete """{"feeds": "OK"}"""
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
  case class SearchUser(query: String, page: Int)
  case class GetFollowers(userId: Int, page: Int)
  case class GetFollowings(userId: Int, page: Int)
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
    case SearchUser(query, page) =>
      UserDAO.searchUser(query, page) pipeTo sender
    case GetFollowers(userId, page) =>
      UserDAO.getFollowers(userId, page) pipeTo sender
    case GetFollowings(userId, page) =>
      UserDAO.getFollowings(userId, page) pipeTo sender
    case GetRelationship(userId, otherId) =>
      UserDAO.getRelationship(userId, otherId) pipeTo sender
    case PostRelationship(userId, otherId) =>
      UserDAO.getRelationship(userId, otherId) flatMap(curRel =>
        UserDAO.postRelationship(userId, otherId, curRel)
        ) pipeTo sender
  }
}
