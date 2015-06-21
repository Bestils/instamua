package com.htvu.instamua.test.persistent
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._
import slick.driver.MySQLDriver.api._
import com.htvu.instamua.rest.dao._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.MySQLDriver.api._
import scala.concurrent.duration._

//this is for testing general slick queries and future; for specific Dao, create corresponding test like UserDaoSpec, etc...
class SlickDaoSpec extends FlatSpec with BeforeAndAfterEach with ScalaFutures with MustMatchers {
  var dbs: Database = _
  var userDao: UserDAO = _
  val users = TableQuery[Users]
  
  def cleanData():Unit = {
    //truncate table before each test
    val deleteSeq: Seq[DBIO[Int]] = Seq(
      sqlu"""delete from user"""
    )

    //convert it into DBIO[Seq[Int]] to run in parallel
    val parallelSeq: DBIO[Seq[Int]] = DBIO.sequence(deleteSeq)

    //run without getting result
    val f:Future[Seq[Int]] = dbs.run(parallelSeq)
    Await.result(f, Duration.Inf)
  }
  
  //setup connection; and pass-it into dao object
  override def beforeEach(): Unit = {
    dbs = Database.forConfig("jdbc-test")
    userDao = new UserDAO{override val db = dbs}

    cleanData()
  }

  "Slick" must "be able to insert and find result" in {
    val f1:Future[Int] = dbs.run(users returning users.map(_.id) += User(1, "test_user", Some("test user"), None, None, None, None, None))
    def findUser(id: Int):Future[Option[User]] = {
      userDao.getUserInfo(id)
    }

    val f2:Future[Option[User]] = for {
      x <- f1;
      y <- findUser(x)
    } yield y

    //assert some results
    whenReady(f2, timeout(Span(2, Seconds))){ user =>
      assert(user.isEmpty == false);
      assert(user.get.username == "test_user")
      assert(user.get.fullName == Some("test user"))
    }
  }

  //clean data
  override def afterEach(): Unit = {
    cleanData()
    
    userDao.db.close
    dbs.close
  }
}