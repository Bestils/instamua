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

//this trait provide out-of-box authorization functions
trait SessionCookieAuthorizationProvider {
  //simple authorization function to allow access with certain role
  //TODO: the actual implementation will be more complex with checking for special resources
  def withRole(role: Int, sessionData: SessionData): Boolean = {
    sessionData.role match {
      case Some(listRole) => {
        val listRoleIds:List[Int] = listRole.map(roleObj => roleObj.id)
        listRoleIds.contains(RoleType.SUPERADMIN) || listRoleIds.contains(role)
      }
      case None => false
    }
  }
}