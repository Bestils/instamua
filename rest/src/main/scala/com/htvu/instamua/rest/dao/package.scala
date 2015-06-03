package com.htvu.instamua.rest

import com.github.tototoshi.slick.MySQLJodaSupport._
import org.joda.time.DateTime
import slick.driver.MySQLDriver.api._

package object dao {

  type UserSearchResult = (Int, String, Option[String], Option[String])
  type FollowerListResult = (Int, String, Option[String], Option[String], Boolean)

  case class User(
                   id: Int,
                   username: String,
                   fullName: Option[String],
                   location: Option[String],
                   bio: Option[String],
                   website: Option[String],
                   profilePicture: Option[String]
                   )


  class Users(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Int]("user_id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def fullName = column[String]("full_name")
    def location = column[String]("location")
    def bio = column[String]("bio")
    def webSite = column[String]("web_site")
    def profilePicture = column[String]("profile_picture")

    def * = (id, username, fullName.?, location.?, bio.?, webSite.?, profilePicture.?) <> (User.tupled, User.unapply)
  }

  case class UserRegistrationInfo(
                                   username: String,
                                   passwd: String,
                                   email: String,
                                   location: String
                                   )

  case class UserCredential(
                             userId: Int,
                             password: String,
                             salt: String
                             )

  class UserCredentials(tag: Tag) extends Table[UserCredential](tag, "user_credential") {
    def userId = column[Int]("user_id")
    def user = foreignKey("user_credential_user_fk", userId, TableQuery[Users])(_.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete=ForeignKeyAction.Cascade)

    def password = column[String]("password")
    def salt = column[String]("salt")

    def * = (userId, password, salt) <> (UserCredential.tupled, UserCredential.unapply)
  }

  case class UserPrivateInfo (
                               userId: Int,
                               email: String,
                               gender: Option[Byte],
                               dob: Option[DateTime]
                               )

  class UserPrivateInfos(tag: Tag) extends Table[UserPrivateInfo](tag, "user_private_info") {
    def userId = column[Int]("user_id")
    def user = foreignKey("user_private_info_user_fk", userId, TableQuery[Users])(_.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete=ForeignKeyAction.Cascade)

    def email = column[String]("email")
    def gender = column[Byte]("gender")
    def dob = column[DateTime]("dob")

    def * = (userId, email, gender.?, dob.?) <> (UserPrivateInfo.tupled, UserPrivateInfo.unapply)
  }

  case class Follower(
                       userId: Int,
                       followerId: Int,
                       followBack: Boolean
                       )

  class Followers(tag: Tag) extends Table[Follower](tag, "follower") {
    def userId = column[Int]("user_id")
    def followerId = column[Int]("follower_id")
    def followBack = column[Boolean]("follow_back")

    def user = foreignKey("follower_user_fk", userId, TableQuery[Users])(_.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade
    )
    def follower = foreignKey("follower_follower_fk", followerId, TableQuery[Users])(_.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade
    )

    def * = (userId, followerId, followBack) <> (Follower.tupled, Follower.unapply)
  }

  object Relationship extends Enumeration {
    type Relationship = Value
    val NO_REL, A_TO_B, A_TO_B_BOTH, B_TO_A, B_TO_A_BOTH = Value
  }

  case class Listing(
                    id: String,
                    category: String,
                    price: Float,
                    title: String,
                    description: Option[String],
                    location: String,
                    comments: Seq[Comment],
                    likes: Seq[Like]
                    )
  case class Comment(
                    id: String,
                    text: String,
                    userId: Int,
                    username: String,
                    createdAt: DateTime,
                    profilePicture: String
                    )
  case class Like(
                 id: String,
                 userId: Int,
                 username: String
                 )
}
