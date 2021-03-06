package com.htvu.instamua.rest

import akka.actor._
import net.ceedubs.ficus.Ficus._

import util._

//This is only intended to be used in case we want to retrieve config from the external actor (or when we have ref to actorSystem)
//TODO: check if we really need those following?
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

  //config of nodejs server
  object NodeJs {
    val Url = config.as[String]("nodejs.url")
    val StaticDir = config.as[String]("nodejs.staticDir")
  }
  
  object SSO {
    val Url = config.as[String]("sso.url")
  }
}

//allow access settings from implicit ActorRefFactory/ActorSystem;
//just need to make sure the implicit ActorRefFactory is in scope of inheriting class
trait SettingsProvider extends ActorRefFactoryProvider {
  lazy val settings: Settings = Settings(actorSystem)
}
