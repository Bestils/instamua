package com.htvu.instamua.rest.session

import akka.actor.ActorRefFactory
import akka.util.Timeout

import com.redis.RedisClient
import com.redis.serialization.DefaultFormats._
import com.redis.serialization._
import spray.json.JsonFormat
import com.redis.serialization.DefaultFormats._

import com.typesafe.config.Config
import scala.concurrent.Future

import spray.http.{DateTime, HttpCookie}

class RedisSessionManager[T](config: Config)(
  implicit system: ActorRefFactory,
  timeout: Timeout,
  format: Format[T])
  extends StatefulSessionManager[T](config) {

  import system.dispatcher

  private val client =
    RedisClient(
      host = config.getString("spray.routing.session.redis.host"),
      port = config.getInt("spray.routing.session.redis.port"))

  //create a new sid and save into redis
  def start(): Future[String] = {
    val id = newSid
    
    for {
      true <- client.set(id, "")
      true <- client.expire(id, sessionTimeout.toSeconds.toInt)
    } yield id
  }

  //check if key exist
  def isValid(id: String): Future[Boolean] =
    client.exists(id).flatMap {
      case true =>
        client.expire(id, sessionTimeout.toSeconds.toInt).map(_ => true)
      case false =>
        Future.successful(false)
    }

  //get SessionData from cookies
  def get(id: String): Future[Option[T]] = {
    client.get[T](id).flatMap {
      case Some(sessionObj) =>
        client.expire(id, sessionTimeout.toSeconds.toInt).map(_ => Some(sessionObj))
      case None =>
        Future.successful(None)
    }
  }

  //update or set SessionData from cookie
  def update(id: String, sessionObj: T): Future[Unit] =  {
    client.set(id, sessionObj).flatMap {
      case true =>
        client.expire(id, sessionTimeout.toSeconds.toInt).map(_ => ())
      case false =>
        Future.successful(())
    }
  }

  //just delete the session from redis
  def invalidate(id: String): Future[Unit] =
    for(1 <- client.del(id))
    yield ()

  //generate cookie for current request
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
  def onInvalidate(callback: (String, T) => Unit): Unit =
    throw new UnsupportedOperationException("Redis session manager does not support invalidate callbacks")

  def shutdown(): Unit =
    client.shutdown()
}
