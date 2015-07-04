package com.htvu.instamua.dao

import java.sql.DriverManager

import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.scalatest.{BeforeAndAfterAll, Suite}
import slick.driver.MySQLDriver.api._

import scala.concurrent.Await
import scala.concurrent.duration._

trait MySqlSpec extends Suite with BeforeAndAfterAll {
  private val dbName = getClass.getSimpleName.toLowerCase

  private val driver = "com.mysql.jdbc.Driver"
  private val db = Database.forURL("jdbc:mysql://localhost", user="root", driver=driver)

  Await.result(db.run(sql"drop database if exists #$dbName".as[Int]),1.seconds)
  Await.result(db.run(sql"create database #$dbName".as[Any]), 1.seconds)

  val database = Database.forURL(s"jdbc:mysql://localhost/$dbName", user="root", password="", driver=driver)

  private val conn = new JdbcConnection(DriverManager.getConnection(s"jdbc:mysql://localhost/$dbName?user=root"))
  private val liquibase = new Liquibase("liquibase/master.xml", new ClassLoaderResourceAccessor(), conn)
  liquibase.update(null)

  override def afterAll(): Unit = {
    db.run(sql"drop database $dbName".as[Int])
  }
}
