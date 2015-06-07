package com.htvu.instamua.rest.dao

import com.htvu.instamua.rest.Configs
import reactivemongo.api.collections.default.BSONCollection
import scala.collection.JavaConversions._

trait MongoConnector extends MongoConfig {
  import reactivemongo.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  // gets an instance of the driver
  // (creates an actor system)
  val driver = new MongoDriver
  val connection = driver.connection(HOSTS)

  val db = connection(DATABASE)
  val listings = db[BSONCollection](LISTINGS)
}

trait MongoConfig {
  val HOSTS = Configs.mongo.getStringList("hosts")
  val DATABASE = Configs.mongo.getString("db")
  val LISTINGS = Configs.mongo.getString("collections.listings")
}
