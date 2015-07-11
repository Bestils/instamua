package com.htvu.instamua.rest.dao

import com.htvu.instamua.rest.Configs
import reactivemongo.api.collections.default.BSONCollection
import scala.collection.JavaConversions._

trait MongoConnector extends MongoConfig {
  import reactivemongo.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  // gets an instance of the driver
  // (creates an actor system)
  private val driver = new MongoDriver
  private val connection = driver.connection(HOSTS)

  private val db = connection(DATABASE)

  val listings = db[BSONCollection](LISTINGS)
  val comments = db[BSONCollection](COMMENTS)
}

trait MongoConfig {
  val HOSTS = Configs.mongo.getStringList("hosts")
  val DATABASE = Configs.mongo.getString("db")
  val LISTINGS = Configs.mongo.getString("collections.listings")
  val COMMENTS = Configs.mongo.getString("collections.comments")
}
