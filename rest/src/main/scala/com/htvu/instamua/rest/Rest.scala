package com.htvu.instamua.rest

import akka.actor.{ActorRefFactory, ActorSystem}
import akka.io.IO
import akka.util.Timeout
import com.htvu.instamua.rest.api.RoutedHttpService
import com.htvu.instamua.rest.api.services.{MediaService, ListingService, UserService, AuthService}
import com.htvu.instamua.rest.session.{RedisSessionManager, StatefulSessionManagerDirectives}
import com.typesafe.config.ConfigFactory
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import spray.can.Http
import spray.routing.HttpService
import spray.http.CacheDirectives._
import spray.http.HttpHeaders._
import akka.actor._
import spray.http.StatusCodes._
import spray.routing._
import com.htvu.instamua.rest.util._
import akka.util.Timeout
import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.duration.{
Duration,
SECONDS
}
import spray.json.DefaultJsonProtocol._
import scala.concurrent.Future

import akka.util.Timeout
import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config.ConfigFactory

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
  implicit def actorRefFactory: ActorRefFactory = system
  
  private val userService = new UserService()
  private val listingService = new ListingService()
  private val authService = new AuthService()
  private val mediaService = new MediaService()

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
          listingService.routes ~
          mediaService.routes
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

  implicit val timeout = new Timeout(Duration(2, SECONDS))
  
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
}

object Configs {
  val config = ConfigFactory.load()

  val jdbc = config.getConfig("jdbc")
  val mongo = config.getConfig("mongo")
}

object Rest extends App with BootedCore
