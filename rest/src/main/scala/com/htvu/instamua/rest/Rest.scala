package com.htvu.instamua.rest

import akka.actor.{ActorRefFactory, ActorSystem}
import akka.io.IO
import com.htvu.instamua.rest.api.RoutedHttpService
import com.htvu.instamua.rest.api.services.{ListingService, UserService}
import com.typesafe.config.ConfigFactory
import spray.can.Http
import spray.routing.HttpService



trait BootedCore extends HttpService {
  implicit val system = ActorSystem("user-services")

  private val userService = new UserService()
  private val listingService = new ListingService()

  val routes = {
    pathPrefix("api" / "v1") {
      userService.routes ~
      listingService.routes
    }
  }
  val rootService = system.actorOf(RoutedHttpService.props(routes), "root-service")

  IO(Http)(system) ! Http.Bind(rootService, "0.0.0.0", 8080)

  implicit def actorRefFactory: ActorRefFactory = system
}

object Configs {
  private val c = ConfigFactory.load()

  val jdbc = c.getConfig("jdbc")
  val mongo = c.getConfig("mongo")
}

object Rest extends App with BootedCore
