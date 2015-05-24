package com.htvu.instamua.user.core

import akka.actor.{Actor, ActorRef, Props}
import com.htvu.instamua.user.dao._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

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
  case class RequestFollowing(userId: Int, otherId: Int)

  def props(): Props = Props(new UserActor())
}

class UserActor extends Actor {
  import UserActor._

  def receive: Receive = {
    case RegisterNewUser(userInfo: UserRegistrationInfo) =>
      UserDAO.createNewUser(userInfo) onComplete replyToSender(sender())
    case GetUserInfo(userId) =>
      UserDAO.getUserInfo(userId) onComplete replyToSender(sender())
    case GetUserPrivateInfo(userId) =>
      UserDAO.getUserPrivateInfo(userId) onComplete replyToSender(sender())
    case UpdateUserInfo(newUserInfo) =>
      UserDAO.updateUserInfo(newUserInfo) onComplete replyToSender(sender())
    case UpdateUserPrivateInfo(newUserPrivateInfo: UserPrivateInfo) =>
      UserDAO.updateUserPrivateInfo(newUserPrivateInfo) onComplete replyToSender(sender())
    case SearchUser(query) =>
      UserDAO.searchUser(query) onComplete replyToSender(sender())
    case GetFollowers(userId) =>
      UserDAO.getFollowers(userId) onComplete replyToSender(sender())
    case GetFollowings(userId) =>
      UserDAO.getFollowings(userId) onComplete replyToSender(sender())
    case GetRelationship(userId, otherId) =>
      UserDAO.getRelationship(userId, otherId) onComplete replyToSender(sender())
    case RequestFollowing(userId, otherId) =>
      UserDAO.requestFollowing(userId, otherId) onComplete replyToSender(sender())
  }

  private def replyToSender(sender: ActorRef)(message: Try[_]) = message match {
    case Success(value) => sender ! value
    case Failure(t) => sender ! akka.actor.Status.Failure(t)
  }
}
