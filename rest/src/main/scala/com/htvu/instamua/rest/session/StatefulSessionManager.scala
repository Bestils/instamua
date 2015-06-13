package com.htvu.instamua.rest.session

import scala.concurrent.Future
import scala.concurrent.duration.Duration

import spray.http.HttpCookie
import spray.util.pimps.PimpedConfig

import com.typesafe.config.Config

abstract class StatefulSessionManager[T](val config: Config) extends CookieManager[String] {
  val sessionTimeout: Duration =
    new PimpedConfig(config).getDuration("spray.routing.session.timeout")

  //generate unique uuid
  protected def newSid(): String = {
    java.util.UUID.randomUUID().toString().replace("-","")
  }

  def start(): Future[String]

  def isValid(id: String): Future[Boolean]

  def get(id: String): Future[Option[T]]

  def update(id: String, sessionObj: T): Future[Unit]

  def invalidate(id: String): Future[Unit]

  def cookify(payload: String): Future[HttpCookie]

  def onInvalidate(callback: (String, T) => Unit): Unit

  def shutdown(): Unit

}
