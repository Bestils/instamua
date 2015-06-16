package com.htvu.instamua.rest

import com.github.tototoshi.slick.MySQLJodaSupport._
import org.joda.time.DateTime
import reactivemongo.bson._
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
                   profilePicture: Option[String],
                   roleId: Option[Int]
                   )

  class Users(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Int]("user_id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def fullName = column[String]("full_name")
    def location = column[String]("location")
    def bio = column[String]("bio")
    def webSite = column[String]("web_site")
    def profilePicture = column[String]("profile_picture")
    def roleId = column[Int]("role_id")
    
    def * = (id, username, fullName.?, location.?, bio.?, webSite.?, profilePicture.?, roleId.?) <> (User.tupled, User.unapply)
  }

  //one user can have multiple roles
  object RoleType extends Enumeration {
    type Role = Value
    val USER, ADMIN, SUPERADMIN = Value
  }

  case class Role(id: Int, description: String)
  
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

  implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    def read(time: BSONDateTime) = new DateTime(time.value)
    def write(jdtime: DateTime) = BSONDateTime(jdtime.getMillis)
  }

  case class Comment(
                    id: Option[String],
                    text: String,
                    userId: Int,
                    username: String,
                    profilePicture: String
                    )
  implicit val commentFormat = Macros.handler[Comment]

  case class Like(id: Option[String],
                 userId: Int,
                 username: String
                 )
  implicit val likeFormat = Macros.handler[Like]

  case class ListingDetail(
                          category: String,
                          price: Double,
                          title: String,
                          description: Option[String],
                          location: String
                          )
  implicit val listingDetailFormat = Macros.handler[ListingDetail]

  case class Image(url: String, width: Int, height: Int)
  implicit val imageFormat = Macros.handler[Image]

  case class Media(
                    thumbnail: Option[Image] = None,
                    standard: Option[Image] = None
                    )
  implicit val mediaFormat = Macros.handler[Media]

  case class Listing(
                      _id: Option[BSONObjectID],
                      details: ListingDetail,
                      comments: List[Comment],
                      likes: List[Like],
                      medias: List[Media]
                      )
  implicit val listingFormat = Macros.handler[Listing]

  case class CommentProjection(
                            id: Option[String],
                            comments: List[Comment]
                            )
  implicit val commentProjFormat = Macros.handler[CommentProjection]

  case class LikeProjection(
                         id: Option[String],
                         likes: List[Like]
                         )
  implicit val likeProjFormat = Macros.handler[LikeProjection]
}
