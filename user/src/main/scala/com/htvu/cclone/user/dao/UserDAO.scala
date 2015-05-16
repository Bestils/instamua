package com.htvu.cclone.user.dao

import org.joda.time.DateTime

import scala.concurrent.Future
import slick.driver.MySQLDriver.api._
import com.github.tototoshi.slick.MySQLJodaSupport._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

case class User(
               id: Int,
               username: String,
               fullName: String,
               location: Option[String],
               bio: Option[String],
               website: Option[String],
               profilePicture: Option[String]
             )



class Users(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Int]("user_id", O.PrimaryKey, O.AutoInc)
  def username = column[String]("username")
  def fullName = column[String]("full_name")
  def location = column[String]("location")
  def bio = column[String]("bio")
  def webSite = column[String]("web_site")
  def profilePicture = column[String]("profile_picture")

  def * = (id, username, fullName, location.?, bio.?, webSite.?, profilePicture.?) <> (User.tupled, User.unapply)
}

case class UserPrivateInfo (
                             userId: Int,
                             email: String,
                             gender: Option[Byte],
                             dob: Option[DateTime]
                             )

class UserPrivateInfos(tag: Tag) extends Table[UserPrivateInfo](tag, "user_private_infos") {
  def userId = column[Int]("user_id")
  def user = foreignKey("user_fk", userId, TableQuery[Users])(_.id,
    onUpdate = ForeignKeyAction.Restrict,
    onDelete=ForeignKeyAction.Cascade)

  def email = column[String]("email")
  def gender = column[Byte]("gender")
  def dob = column[DateTime]("dob")

  def * = (userId, email, gender.?, dob.?) <> (UserPrivateInfo.tupled, UserPrivateInfo.unapply)
}

object UserDAO {
  val db = Database.forConfig("jdbc")

  val users = TableQuery[Users]
  val userPrivateInfos = TableQuery[UserPrivateInfos]

  // UserInfoService
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

  def getFeed: Future[List[Any]] = ???

  def getRecentListings: Future[List[Any]] = ???

  def searchUser(query: String): Future[List[User]] = ???


  // RelationshipService
  def getFollows(user: User): Future[List[User]] = ???

  def getFollowedBy(user: User): Future[List[User]] = ???

  def getRelationship(user: User): Future[List[User]] = ???

  def requestRelationship(user: User): Future[List[User]] = ???

  def getRequestedBy(user: User): Future[List[User]] = ???
}
