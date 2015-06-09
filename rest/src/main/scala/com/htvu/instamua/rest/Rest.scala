package com.htvu.instamua.rest

import akka.actor.{ActorRefFactory, ActorSystem}
import akka.io.IO
import com.htvu.instamua.rest.api.RoutedHttpService
import com.htvu.instamua.rest.api.services.{ListingService, UserService, AuthService}
import com.typesafe.config.ConfigFactory
import spray.can.Http
import spray.routing.HttpService
import spray.http.CacheDirectives._
import spray.http.HttpHeaders._
import akka.actor._
import spray.http.StatusCodes._
import spray.routing._
import com.htvu.instamua.rest.util._

//some static page directives
trait PageDirectives extends Directives with ActorRefFactoryProvider {
  def completeWithNotFoundPage(): Route = {
    respondWithStatus(NotFound) {
      getFromResource("statics/errors/404.html")
    }
  }

  def completeWithInternalServerErrorPage(): Route = {
    respondWithStatus(InternalServerError) {
      getFromResource("statics/errors/500.html")
    }
  }
}

trait BootedCore extends HttpService with HttpsDirectives with SettingsProvider with PageDirectives{
  implicit val system = ActorSystem("user-services")

  private val userService = new UserService()
  private val listingService = new ListingService()
  private val authService = new AuthService()

  //different rejection and exception handling go here
  //TODO: custom rejection handler REST format
  val ApiRejectionHandler = RejectionHandler.Default
  val noCachingAllowed = respondWithHeaders(RawHeader("Pragma", "no-cache"), `Cache-Control`(`no-store`))

  //rejection handler for static website
  val WebsiteRejectionHandler = RejectionHandler {
    case Nil ⇒ completeWithNotFoundPage()
    case _   ⇒ completeWithInternalServerErrorPage()
  }

  //exception handler
  val WebsiteExceptionHandler = ExceptionHandler {
    case _ ⇒ completeWithInternalServerErrorPage()
  }
  
  val apiRoutes = {
    pathPrefix("api" / "v1") {
      handleRejections(ApiRejectionHandler) {
        noCachingAllowed {
          userService.routes ~
            authService.routes ~
            listingService.routes

        }
      }
    }
  }
  //serve static file from certain root
  val websiteRoutes = {
    handleExceptions(WebsiteExceptionHandler) {
      handleRejections(WebsiteRejectionHandler) {
        getFromResourceDirectory("statics")
      }
    }
  }

  val decompressCompressIfRequested = (decompressRequest() & compressResponseIfRequested())
  
  //merge both routes together + enforce https if needed
  val routes = {
    decompressCompressIfRequested {
      enforceHttpsIf(settings.Http.EnforceHttps) {
        apiRoutes ~ websiteRoutes
      }
    }
  }
  
  //TODO: change wrapper of route so that different exception/rejection handlers for API vs Static Routes
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
