package com.htvu.cclone.user.core

import akka.actor.{Actor, ActorRef, Props}
import com.htvu.cclone.user.dao.{UserPrivateInfo, User, UserDAO}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object UserInfoActor {
  case class GetUserInfo(userId: Int)
  case class UpdateUserInfo(newInfo: User)
  case class UpdateUserPrivateInfo(newPrivateInfo: UserPrivateInfo)

  def props(): Props = Props(new UserInfoActor())
}

class UserInfoActor extends Actor {
  import UserInfoActor._

  def receive: Receive = forwardToDAO(sender())

  def forwardToDAO(sender: ActorRef): Receive = {
    case GetUserInfo(userId) =>
      UserDAO.getUserInfo(userId) onComplete new Foo[Option[User]] {}.replyToSender(sender)
    case UpdateUserInfo(newUserInfo: User) =>
      UserDAO.updateUserInfo(newUserInfo) onComplete new Foo[Try[Int]] {}.replyToSender(sender)
    case UpdateUserPrivateInfo(newUserPrivateInfo: UserPrivateInfo) =>
      UserDAO.updateUserPrivateInfo(newUserPrivateInfo) onComplete new Foo[Try[Int]] {}.replyToSender(sender)
  }

  trait Foo[T] {
    def replyToSender(sender: ActorRef)(message: Try[T]): Unit = message match {
      case Success(value) => sender ! value
      case Failure(t) => sender ! akka.actor.Status.Failure(t)
    }
  }

}
