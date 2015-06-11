package com.htvu.instamua.rest.session

import akka.actor.ActorRefFactory
import akka.util.Timeout

import com.redis.RedisClient
import com.redis.serialization.SprayJsonSupport._
import com.redis.serialization.DefaultFormats._

import spray.json.JsonFormat
import spray.json.DefaultJsonProtocol._

import com.typesafe.config.Config

import java.util.concurrent.TimeUnit

import scala.concurrent.Future
import scala.concurrent.duration._

import spray.http.{DateTime, HttpCookie}

class RedisSessionManager[T](config: Config)(
  implicit system: ActorRefFactory,
  timeout: Timeout,
  format: JsonFormat[T])
  extends StatefulSessionManager[T](config) {

  import system.dispatcher

  private val client =
    RedisClient(
      host = config.getString("spray.routing.session.redis.host"),
      port = config.getInt("spray.routing.session.redis.port"))

  def start(): Future[String] = {
    val id = newSid
    for {
      true <- client.set(id, Map.empty[String, T])
      true <- client.expire(id, sessionTimeout.toSeconds.toInt)
    } yield id
  }

  def get(id: String): Future[Option[Map[String, T]]] =
    client.get[Map[String, T]](id).flatMap {
      case Some(map) =>
        client.expire(id, sessionTimeout.toSeconds.toInt).map(_ => Some(map))
      case None =>
        Future.successful(None)
    }

  def isValid(id: String): Future[Boolean] =
    client.exists(id).flatMap {
      case true =>
        client.expire(id, sessionTimeout.toSeconds.toInt).map(_ => true)
      case false =>
        Future.successful(false)
    }

  def update(id: String, map: Map[String, T]): Future[Unit] =
    client.set(id, map).flatMap {
      case true =>
        client.expire(id, sessionTimeout.toSeconds.toInt).map(_ => ())
      case false =>
        Future.successful(())
    }

  def invalidate(id: String): Future[Unit] =
    for(1 <- client.del(id))
    yield ()

  def cookify(id: String): Future[HttpCookie] =
    for(maxAge <- client.ttl(id))
    yield
      if(maxAge <= -2)
      // unknown session
        HttpCookie(name = cookieName, content = "", maxAge = Some(-1), path = cookiePath, domain = cookieDomain, secure = cookieSecure, httpOnly = cookieHttpOnly )
      else if(maxAge == -1)
      // no ttl for this key
        HttpCookie(name = cookieName, content = id, path = cookiePath, domain = cookieDomain, secure = cookieSecure, httpOnly = cookieHttpOnly )
      else
        HttpCookie(name = cookieName, content = id, maxAge = Some(maxAge), path = cookiePath, domain = cookieDomain, secure = cookieSecure, httpOnly = cookieHttpOnly )

  /** This operation is not supported for Redis session manager */
  def onInvalidate(callback: (String, Map[String, T]) => Unit): Unit =
    throw new UnsupportedOperationException("Redis session manager does not support invalidate callbacks")

  def shutdown(): Unit =
    client.shutdown()
}
