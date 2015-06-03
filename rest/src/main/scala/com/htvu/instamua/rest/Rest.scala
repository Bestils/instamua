package com.htvu.instamua.rest

import akka.actor.{ActorRefFactory, ActorSystem}
import akka.io.IO
import com.htvu.instamua.rest.api.RoutedHttpService
import com.htvu.instamua.rest.api.services.UserService
import com.typesafe.config.ConfigFactory
import spray.can.Http
import spray.routing.HttpService



trait BootedCore extends HttpService {
  implicit val system = ActorSystem("user-services")

  private val userInfoService = new UserService()

  val routes = {
    pathPrefix("api" / "v1") {
      userInfoService.routes
    }
  }
  val rootService = system.actorOf(RoutedHttpService.props(routes), "root-service")

  IO(Http)(system) ! Http.Bind(rootService, "0.0.0.0", 8080)

  implicit def actorRefFactory: ActorRefFactory = system
}

object Configs {
  private val c = ConfigFactory.load()

  val jdbc = c.getConfig("jdbc")
}

object Rest extends App with BootedCore
