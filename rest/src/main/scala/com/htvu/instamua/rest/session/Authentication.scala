package com.htvu.instamua.rest.session
import spray.routing.{HttpService, RequestContext, AuthenticationFailedRejection}
import concurrent.Future
import spray.routing.authentication.Authentication
import com.htvu.instamua.rest.dao._
import scala.concurrent._

//what you want to store in the session
case class SessionData(user: Option[User], role: Option[Role])

class AuthController {

  /**function to do the authentication to establish user identity
    */
  def doAuth(sessionId: String): Option[SessionData] = {
    //Here, we are just creating and returning plain object. Insert
    //your DB and caching logic here.

    //Your real logic could be something like this: if you don't find
    //in the DB this token to be associated to any real user, return
    //None, otherwise, Auth info associated with that token.
    return None
  }
}

//implement authorization logics
trait AuthenticationDirectives {
  this: HttpService =>

  val authController: AuthController

  //this will query database (redis, mongo or mysql) to fill in session data
  def doAuthenticate(password: String)(implicit ec: ExecutionContext): Future[Option[SessionData]]
  
  //only authorize user with certain role
  def withRole(role: Int)(implicit ec: ExecutionContext) : RequestContext => Future[Authentication[SessionData]] = {
    ctx: RequestContext =>
      val sessionId = getSessionId(ctx)
      if (sessionId.isEmpty)
        Future(Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsMissing, List())))
      else doAuthenticate(sessionId.get).map {
        auth =>
          if (auth.isDefined && auth.get.role.get.id == role)
            Right(auth.get)
          else
            //invalid role; we reject the authentication credential
            Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, List()))
      }
  }

  //extract session id from request cookie
  def getSessionId(ctx: RequestContext): Option[String] = {
    //get session id; check the signed condition?
    Some("mock:sid")
  }
}

trait UsersAuthenticationDirectives
  extends AuthenticationDirectives {
  this: HttpService =>

  val authController = new AuthController

  override def doAuthenticate(sessionId: String)(implicit ec: ExecutionContext) = {
    Future {authController.doAuth(sessionId)}
  }
}