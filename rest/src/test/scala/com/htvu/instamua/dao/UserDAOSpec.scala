package com.htvu.instamua.dao

import java.util.UUID

import com.htvu.instamua.rest.dao._
import org.scalatest.{BeforeAndAfterEach, FeatureSpec, GivenWhenThen}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class UserDAORelationshipSpec extends FeatureSpec with MySqlSpec with GivenWhenThen with BeforeAndAfterEach {
  val userDAO = new UserDAO() {
    override val db = database
  }
  import Relationship._

  info("as an user")
  info("I want to be able to follow/unfollow other user")
  info("I want to be able to follow back a follower")
  info("I want to see all followers of an user")
  info("I want to see all users that an user follows")

  var A = 1
  var B = 2

  override def beforeEach(): Unit = {
    val usernameA = UUID.randomUUID().toString.substring(0, 30)
    val usernameB = UUID.randomUUID().toString.substring(0, 30)
    A = Await.result(userDAO.createNewUser(UserRegistrationInfo(usernameA, "password", "a.test.com", "vietnam", "Vu", A)), 1.seconds)
    B = Await.result(userDAO.createNewUser(UserRegistrationInfo(usernameB, "password", "b.test.com", "vietnam", "Ho", B)), 1.seconds)
  }

  feature("follow/unfollow") {
    scenario("follow non-follower") {
      Given("A & B not following each other")
      assert(Await.result(userDAO.getRelationship(A, B), 1.seconds) === NO_REL)
      assert(Await.result(userDAO.getRelationship(B, A), 1.seconds) === NO_REL)

      When("A follow B")
      userDAO.postRelationship(A, B, NO_REL)

      Then("A should be follower of B but not vise versa")
      assert(Await.result(userDAO.getRelationship(A, B), 1.seconds) === A_TO_B)
    }

    scenario("follow back") {
      Given("B already follow A")
      userDAO.postRelationship(B, A, NO_REL)
      assert(Await.result(userDAO.getRelationship(A, B), 1.seconds) === B_TO_A)

      When("A follow B")
      Await.result(userDAO.postRelationship(A, B, B_TO_A), 1.seconds)

      Then("A& B should follow each other")
      assert(Await.result( userDAO.getRelationship(A, B), 1.seconds) === B_TO_A_BOTH)
    }


    scenario("unfollow a follower") {
      Given("A and B follow each other")
      userDAO.postRelationship(A, B, NO_REL)
      assert(Await.result(userDAO.getRelationship(A, B), 1.seconds) === A_TO_B)
      userDAO.postRelationship(B, A, B_TO_A) // note that userId 'A' corresponds to 'B' in the relationship
      assert(Await.result(userDAO.getRelationship(A, B), 1.seconds) === A_TO_B_BOTH)

      When("A unfollow B")
      Await.result(userDAO.postRelationship(A, B, A_TO_B_BOTH), 1.seconds)

      Then("only B should follow A")
      assert(Await.result(userDAO.getRelationship(A, B), 1.seconds ) === B_TO_A)
    }

    scenario("unfollow a non-follower") {
      Given("A already follow B but not vise versa")
      userDAO.postRelationship(A, B, NO_REL)
      assert(Await.result(userDAO.getRelationship(A, B), 1.seconds) === A_TO_B)

      When("A unfollow B")
      Await.result(userDAO.postRelationship(A, B, A_TO_B), 1.seconds)

      Then("A B should have no relationship")
      assert(Await.result(userDAO.getRelationship(A, B), 1.seconds) === NO_REL)
    }

  }

  feature("see followers/followings of an user") {
    def followers(uid: Int) = Await.result(userDAO.getFollowers(uid), 1.seconds) map (_._1)
    def followings(uid: Int) = Await.result(userDAO.getFollowings(uid), 1.seconds) map (_._1)

    scenario("after an new user follow") {
      Given("A is not following B")
      assert(followers(B).contains(A) === false)
      assert(followings(A).contains(B) === false)

      When("A follow B")
      Await.result(userDAO.postRelationship(A, B, NO_REL), 1.seconds)

      Then("A should be in B's followers")
      assert(followers(B) contains A)

      Then("B should be in A's followings")
      assert(followings(A) contains B)
    }

    scenario("after an user unfollow") {
      Given("A follows B")
      Await.result(userDAO.postRelationship(A, B, NO_REL), 1.seconds)
      assert(followers(B) contains A)
      assert(followings(A) contains B)

      When("A unfollow B")
      Await.result(userDAO.postRelationship(A, B, A_TO_B), 1.seconds)

      Then("A should not be in B's followers")
      assert(followers(B).contains(A) === false)

      Then("B should not be in A's followings")
      assert(followings(A).contains(B) === false)
    }
  }
}


class UserDAOCRUDSpec extends FeatureSpec with MySqlSpec with GivenWhenThen with BeforeAndAfterEach {

  val userDAO = new UserDAO() {
    override val db = database
  }

  info("as an user")
  info("I want to be able to register a new account")
  info("I want to be able to update my information")
  info("I want to be able to see an user public information")
  info("I want to see all users that an user follows")
  info("I want to see be able to search for a particular using username or full name")

  feature("User registration") {
    // since the actual verification is handled by external auth service
    // we can safely assume that the user we are about to create here is always valid
    scenario("User is not existent") {
      Given("User AAA is not created")
      assert(Await.result(userDAO.searchUser("AAA"), 1.seconds).isEmpty)

      When("Created")
      val userId = Await.result(userDAO.createNewUser(UserRegistrationInfo("AAA", "password", "AAA@test.com", "vietnam", "A A A", 1)), 1.seconds)

      Then("User AAA should be created")
      assert(userId > -1)
      assert(Await.result(userDAO.getUserInfo(userId), 1.seconds) === Some(User(userId, "AAA", Some("A A A"), Some("vietnam"), None, None, None, None, Some(1))))
    }
  }

  feature("Update user information") {
    scenario("Update info of existing user") {
      Given("An existing user")
      val userId = Await.result(userDAO.createNewUser(UserRegistrationInfo("ronaldo", "password", "ronaldo@test.com", "brazil", "Ronaldo", 1)), 1.seconds)
      val newUser = Some(User(userId, "ronaldo", Some("Ronaldo"), Some("brazil"), None, None, None, None, Some(1)))
      assert(Await.result(userDAO.getUserInfo(userId), 1.seconds) === newUser)

      When("Update user info")
      val updated = User(userId, "ronaldo", Some("Ronaldo Beo"), Some("brazil"),
        Some("I'm a footballer"), Some("ronaldo.com"), Some("google.com/ronaldo.jpg"), None, Some(1))
      Await.result(userDAO.updateUserInfo(updated), 1.seconds)

      assert(Await.result(userDAO.getUserInfo(userId), 1.seconds) === Some(updated))
    }
  }

  feature("Get user public information") {
    scenario("User exists") {
      Given("An existing user")
      val userId = Await.result(userDAO.createNewUser(UserRegistrationInfo(
        "nguyenxuantuong", "password", "nxt@test.com", "hanoi", "Nguyen Xuan Tuong", 1)), 1.seconds)

      When("Query user info")
      val user = Await.result(userDAO.getUserInfo(userId), 1.seconds)

      Then("It should return user public information")
      val result = User(userId, "nguyenxuantuong", Some("Nguyen Xuan Tuong"), Some("hanoi"), None, None, None, None, Some(1))
      assert(user === Some(result))
    }

    scenario("User does not exists") {
      When("Query and non-existent userId")
      val user = Await.result(userDAO.getUserInfo(123), 1.seconds)
      assert(user === None)
    }
  }

  feature("Search for user with username or password") {
    val uid1 = Await.result(userDAO.createNewUser(UserRegistrationInfo("hotienvu", "password", "hotienvu@test.com", "vietnam", "Ho Tien Vu")), 1.seconds)
    val user1 = (uid1, "hotienvu", Some("Ho Tien Vu"), None)

    scenario("Search by username") {
      When("Username contains query string")
      val results = Await.result(userDAO.searchUser("tien"), 1.seconds)

      Then("should return one user")
      assert(results === Vector(user1))
    }

    scenario("Search by full name") {
      When("User full name contains query string")
      val results = Await.result(userDAO.searchUser("Tien"), 1.seconds)

      Then("should return one user")
      assert(results === Vector(user1))
    }

    scenario("Return multiple results") {
      Given("When there are 2 users with very similar username/fullname")
      val uid2 = Await.result(userDAO.createNewUser(UserRegistrationInfo("temp", "password", "hotienvu@test.com", "vietnam", "Vu Ho")), 1.seconds)
      val user2 = (uid2, "temp", Some("Vu Ho"), None)

      When("Search for these users")
      val results = Await.result(userDAO.searchUser("Vu"), 1.seconds)

      Then("It should return all that match")
      assert(results === Seq(user1, user2))
    }

    scenario("No result") {
      When("Query string doesn't match any user")
      val results = Await.result(userDAO.searchUser("vuu"), 1.seconds)

      Then("It should return an empty list")
      assert(results.isEmpty)
    }
  }
}