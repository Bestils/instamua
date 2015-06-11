package com.htvu.instamua.rest.session

import com.typesafe.config.Config
import spray.http.HttpCookie

import scala.concurrent.Future

trait CookieManager[T] {
    def config : Config

    val cookieName: String =
        config.getString("spray.routing.session.cookie.name")

    val cookieDomain: Option[String] =
        config.getString("spray.routing.session.cookie.domain") match {
            case s : String if !s.isEmpty()  => Some(s)
            case _ => None
        }

    val cookiePath: Option[String] =
        config.getString("spray.routing.session.cookie.path") match {
            case s : String if !s.isEmpty()  => Some(s)
            case _ => None
        }

    val cookieSecure: Boolean =
        config.getBoolean("spray.routing.session.cookie.secure")

    val cookieHttpOnly: Boolean =
        config.getBoolean("spray.routing.session.cookie.httpOnly")

    def cookify(payload : T): Future[HttpCookie]
}
