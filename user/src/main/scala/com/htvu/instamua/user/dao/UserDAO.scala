package com.htvu.instamua.user.dao

import com.github.tototoshi.slick.MySQLJodaSupport._
import org.joda.time.DateTime
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.util.Try


object UserDAO {
  val db = Database.forConfig("jdbc")

  type UserSearchResult = (Int, String, String, String)
  type FollowerListResult = (Int, String, String, String, Boolean)

  val users = TableQuery[Users]
  val userPrivateInfos = TableQuery[UserPrivateInfos]
  val userCredentials = TableQuery[UserCredentials]
  val followers = TableQuery[Followers]


  def createNewUser(userInfo: UserRegistrationInfo): Future[User] = ???

  def getUserInfo(userId: Int): Future[Option[User]] =
    db.run(users.filter(_.id === userId).take(1).result.headOption)


  def getUserPrivateInfo(userId: Int): Future[Option[UserPrivateInfo]] =
    db.run(userPrivateInfos.filter(_.userId === userId).take(1).result.headOption)

  def updateUserInfo(newInfo: User): Future[Try[Int]] = {
    val updateUserInfo = users.filter(_.id === newInfo.id)
    db.run(updateUserInfo.update(newInfo).asTry)
  }

  def updateUserPrivateInfo(newPrivateInfo: UserPrivateInfo): Future[Try[Int]] = {
    val updateUserPrivateInfo = userPrivateInfos.filter(_.userId === newPrivateInfo.userId)
    db.run(updateUserPrivateInfo.update(newPrivateInfo).asTry)
  }

  def searchUser(query: String): Future[Seq[UserSearchResult]] =
    db.run(sql"""select user_id, username, full_name, profile_picture
           from user where username like '%#$query%' or full_name like '%#$query%'""".as[UserSearchResult])

  // RelationshipService
  def getFollowers(userId: Int): Future[Seq[FollowerListResult]] = {
    val query = for {
      follower <- followers.filter(_.userId === userId)
      user <- users if user.id === follower.followerId
    } yield (user.id, user.username, user.fullName, user.profilePicture, follower.followBack)
    db.run(query.result)
  }

  def getFollowings(userId: Int): Future[Seq[FollowerListResult]] = {
    val query = for {
      following <- followers.filter(_.followerId === userId)
      user <- users if user.id === following.userId
    } yield (user.id, user.username, user.fullName, user.profilePicture, following.followBack)
    db.run(query.result)
  }

  def getRelationship(userId: Int, otherId: Int): Future[Boolean] =
    db.run(followers.filter(_.userId === otherId).filter(_.followerId === userId).take(1).result.headOption) map (_ exists (_ => true))

  def requestFollowing(userId: Int, otherId: Int): Future[Boolean] = ???
}
