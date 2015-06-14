package com.htvu.instamua.rest

import akka.actor._
import net.ceedubs.ficus.Ficus._

import util._

object Settings extends ExtensionKey[Settings]

class Settings(system: ExtendedActorSystem) extends Extension {
  private val config = system.settings.config
  
  //http settings
  object Http {
    val Port = config.as[Int]("app.port")
    val EnforceHttps = config.as[Boolean]("app.enforce-https")
  }
  
  //Other settings go here
  object Session {
    val CookieSessionName:String = config.as[String]("spray.routing.session.cookie.name")
    val CookieCSRFName:String = config.as[String]("spray.routing.session.cookie.csrfName")
  }
}

trait SettingsProvider extends ActorRefFactoryProvider {
  lazy val settings: Settings = Settings(actorSystem)
}
