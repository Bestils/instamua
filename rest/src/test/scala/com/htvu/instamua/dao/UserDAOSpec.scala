package com.htvu.instamua.dao

import java.util.UUID

import com.htvu.instamua.rest.dao.{Relationship, UserDAO, UserRegistrationInfo}
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
    A = Await.result(userDAO.createNewUser(UserRegistrationInfo(usernameA, "p@ssw00rd", "a.test.com", "vietnam")), 1.seconds)
    B = Await.result(userDAO.createNewUser(UserRegistrationInfo(usernameB, "p@ssw00rd", "b.test.com", "vietnam")), 1.seconds)
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