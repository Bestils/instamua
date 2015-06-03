package com.htvu.instamua.rest.core

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import com.htvu.instamua.rest.dao._

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

class UserActor extends Actor {
  import UserActor._
  implicit val exec = context.dispatcher

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
