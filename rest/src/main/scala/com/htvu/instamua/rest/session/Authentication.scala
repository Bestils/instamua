package com.htvu.instamua.rest.session

import akka.util.Timeout
import com.htvu.instamua.rest.SettingsProvider
import com.htvu.instamua.rest.api.JsonSessionFormat
import com.typesafe.config.ConfigFactory
import spray.routing.{HttpService, RequestContext, AuthenticationFailedRejection}
import concurrent.Future
import spray.routing.authentication._
import com.htvu.instamua.rest.dao._
import scala.concurrent._
import scala.concurrent.duration._

//what you want to store in the session
case class SessionData(user: Option[User], role: Option[List[Role]])

abstract class Authenticator extends ContextAuthenticator[SessionData] {
  /**
   * Function to make Authenticators composable, i.e. to create a new Authenticator
   * that wraps two others and that will try the second one if the first one fails
   * to authenticate the request.
   */
  def orElse(other: Authenticator)(implicit ec: ExecutionContext): Authenticator = {
    new Authenticator {
      def apply(requestContext: RequestContext): Future[Authentication[SessionData]] = {
        // We need to explicitly specify the 'super' apply method from the surrounding
        // class so we can call it without calling ourselves recursively by accident
        Authenticator.this.apply(requestContext).flatMap {
          case success @ Right(_) ⇒ Future.successful(success)
          case Left(rejection)    ⇒ other.apply(requestContext)
        }
      }
    }
  }
}

//this provide authentication method
trait SessionCookieAuthenticatorProvider extends StatefulSessionManagerDirectives[SessionData] with JsonSessionFormat with SettingsProvider {
  implicit val timeout = new Timeout(Duration(2, SECONDS))
  import ExecutionContext.Implicits.global
  implicit val manager = new RedisSessionManager[SessionData](ConfigFactory.load())

  val SessionCookieAuthenticator: Authenticator = new SessionCookieAuthenticatorImpl()
  val SessionCookieXsrfAuthenticator: Authenticator = new SessionCookieXsrfAuthenticatorImpl()

  //get session from store and verify that it's valid
  private class SessionCookieAuthenticatorImpl()(implicit ec: ExecutionContext,
                                                 manager: StatefulSessionManager[SessionData]) extends Authenticator {
    def apply(ctx: RequestContext): Future[Authentication[SessionData]] = {
      findSessionIdCookieValue(ctx)
        .map { sessionId ⇒
        validateSessionId(ctx, sessionId)
      }.getOrElse {
        Future.successful(Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, List())))
      }
    }

    //extract session id from request context
    private def findSessionIdCookieValue(ctx: RequestContext): Option[String] =
      ctx.request.cookies.find(_.name == settings.Session.CookieSessionName).map(_.content)

    //valid session id
    private def validateSessionId(ctx: RequestContext, sessionId: String): Future[Authentication[SessionData]] = {
      //get session data from session store; if not exist; then reject the request
      manager.get(sessionId).map {
        case Some(sessionObj) => Right(sessionObj)
        case None => Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, List()))
      }
    }
  }

  private class SessionCookieXsrfAuthenticatorImpl()(implicit ec: ExecutionContext,
                                                     manager: StatefulSessionManager[SessionData]) extends SessionCookieAuthenticatorImpl() {
    val CsrfTokenHeaderName = settings.Session.CookieSessionName

    override def apply(ctx: RequestContext): Future[Authentication[SessionData]] = {
      import ExecutionContext.Implicits.global
      super.apply(ctx).map { authentication ⇒
        authentication.right.flatMap(session ⇒ validateCsrfToken(ctx, session))
      }
    }

    private def findCsrfTokenHeaderValue(ctx: RequestContext): Option[String] =
      ctx.request.headers.find(_.lowercaseName == CsrfTokenHeaderName.toLowerCase).map(_.value)

    private def validateCsrfToken(ctx: RequestContext, session: SessionData): Authentication[SessionData] = {
      findCsrfTokenHeaderValue(ctx).map { csrfToken ⇒
        //TODO: in real logic, we have to verify the csrf token with the one in session data
        val mock = true
        if (mock) {
          Right(session)
        } else {
          Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, List()))
        }
      }.getOrElse {
        Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, List()))
      }
    }
  }
}