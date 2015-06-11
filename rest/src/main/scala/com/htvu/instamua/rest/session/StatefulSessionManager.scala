package com.htvu.instamua.rest.session

import scala.concurrent.Future
import scala.concurrent.duration.Duration

import spray.http.HttpCookie
import spray.util.pimps.PimpedConfig

import com.typesafe.config.Config

abstract class StatefulSessionManager[T](val config: Config) extends CookieManager[String] {

  private val alpha = "abcdefghijklmnopqrstuvwxyz"
  private val symbols = alpha + alpha.toUpperCase + "0123456789/=?+-_:"
  private val symLength = symbols.length
  private val idLength = 16
  private val random = new java.security.SecureRandom

  val sessionTimeout: Duration =
    new PimpedConfig(config).getDuration("spray.routing.session.timeout")

  /** Generates a new identifier */
  protected def newSid(): String = {
    val buf = new StringBuilder
    (1 to idLength).foreach(_ => buf.append(symbols.charAt(random.nextInt(symLength))))
    buf.toString
  }

  def start(): Future[String]

  def isValid(id: String): Future[Boolean]

  def get(id: String): Future[Option[Map[String, T]]]

  def update(id: String, map: Map[String, T]): Future[Unit]

  def invalidate(id: String): Future[Unit]

  def cookify(payload: String): Future[HttpCookie]

  def onInvalidate(callback: (String, Map[String, T]) => Unit): Unit

  def shutdown(): Unit

}
