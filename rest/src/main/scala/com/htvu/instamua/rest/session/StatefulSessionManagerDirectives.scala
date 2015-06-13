package com.htvu.instamua.rest.session

import scala.concurrent.{
  Future,
  ExecutionContext
}

import spray.routing.directives.{
  BasicDirectives,
  CookieDirectives,
  RouteDirectives,
  FutureDirectives
}

import spray.routing._
import spray.http.HttpHeaders

import shapeless._

import scala.language.implicitConversions

//trait contains list of session directives
trait StatefulSessionManagerDirectives[T] extends BasicDirectives with CookieDirectives with RouteDirectives {
  //get current session
  def session(magnet: WithStatefulManagerMagnet[String, T]): Directive1[Option[T]] =
    magnet.directive(_.get(magnet.in))

  //start with fresh session
  def newSession(magnet: WithStatefulManagerMagnet[Unit, T]): Directive1[String] =
    magnet.directive(_.start())

  //update by setting key-value and allow pass
  def updateSession(magnet: WithStatefulManagerMagnet[(String, T), T]): Directive0 =
    magnet.directive(_.update(magnet.in._1, magnet.in._2)).hflatMap(_ => pass)

  //just remove session and allow pass
  def invalidateSession(magnet: WithStatefulManagerMagnet[String, T]): Directive0 =
    magnet.directive(_.invalidate(magnet.in)).hflatMap(_ => pass)

  /** Gets the current session given by the cookie if any.
   *  If no session cookie exists, a new session is started and returned.
   *  If an invalid or expired session identifier is given, the request is rejected */
  def cookieSession(magnet: WithStatefulManagerMagnet[Unit, T]): Directive[String :: T :: HNil] =
    optionalCookie(magnet.manager.cookieName).hflatMap {
      case Some(cookie) :: HNil =>
        magnet.directive(_.get(cookie.content)).hflatMap {
          case Some(sess) :: HNil =>
            hprovide(cookie.content :: sess :: HNil)
          case None :: HNil =>
            // the session does not exist or has expired, reject
            // just start a new one and discard old cookie
            deleteCookie(cookie).hflatMap {
              case HNil =>
                startFresh(magnet)
            }
        }

      case None :: HNil =>
        startFresh(magnet)

    }

  private def startFresh(magnet: WithStatefulManagerMagnet[Unit, T]): Directive[String :: T :: HNil] =
      magnet.directive(_.start()).hflatMap {
        case id :: HNil =>
          magnet.directive(_.get(id)).hflatMap {
            case Some(map) :: HNil =>
              magnet.directive(_.cookify(id)).hflatMap {
                case cookie :: HNil =>
                  (mapRequest(_.withHeaders(HttpHeaders.Cookie(cookie))) & setCookie(cookie)).hmap { _ =>
                    id :: map :: HNil
                  }
              }
            case None :: HNil =>
              reject(InvalidSessionRejection(id))
          }
      }

  def setCookieSession(magnet: WithStatefulManagerMagnet[String, T]): Directive0 =
    magnet.directive(_.cookify(magnet.in)).flatMap {
      case cookie => setCookie(cookie)
    }

}

trait WithStatefulManagerMagnet[In,T] {
  import FutureDirectives._

  implicit val executor: ExecutionContext

  implicit val manager: StatefulSessionManager[T]

  val in: In

  def directive[Out](action: StatefulSessionManager[T] => Future[Out]): Directive1[Out] =
    onSuccess(action(manager))

}

object WithStatefulManagerMagnet {

  implicit def apply[In,T](i: In)(implicit ec: ExecutionContext,
    m: StatefulSessionManager[T]): WithStatefulManagerMagnet[In,T] =
    new WithStatefulManagerMagnet[In,T] {
      implicit val executor = ec
      val manager = m
      val in = i
    }

}
