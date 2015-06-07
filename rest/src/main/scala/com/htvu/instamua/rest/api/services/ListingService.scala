package com.htvu.instamua.rest.api.services

import akka.actor.ActorSystem
import akka.util.Timeout
import com.htvu.instamua.rest.api.JsonFormats
import com.htvu.instamua.rest.core.ListingActor
import com.htvu.instamua.rest.core.ListingActor._
import com.htvu.instamua.rest.dao.{Like, Comment, ListingDetail, Listing}
import spray.routing.Directives
import akka.pattern.ask

class ListingService()(implicit system: ActorSystem) extends Directives with JsonFormats {

  val listingActor = system.actorOf(ListingActor.props(), "listing-actor")
  implicit val ec = system.dispatcher

  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)

  val routes = pathPrefix("listings") {
    pathEnd {
      post {
        handleWith { listing: Listing =>
          listingActor? NewListing(listing)
        }
      }
    } ~
    path("search") {
      get {
        parameter("q") { query =>
          _ complete (listingActor? SearchListing(query)).mapTo[List[Listing]]
        }
      }
    } ~
    pathPrefix(Segment) { listingId =>
      pathEnd {
        get {
          _ complete (listingActor? GetListing(listingId)).mapTo[Option[Listing]]
        } ~
        put {
          handleWith { updated: ListingDetail =>
            listingActor? UpdateListing(listingId, updated)
          }
        } ~
        delete {
          _ complete listingActor? DeleteListing(listingId)
        }
      } ~
      pathPrefix("comments") {
        pathEnd {
          get {
            _ complete (listingActor? GetComments(listingId)).mapTo[Option[List[Comment]]]
          } ~
          post {
            handleWith { comment: Comment =>
              (listingActor? NewComment(listingId, comment)).mapTo[String]
            }
          }
        } ~
        path(Segment) { commentId: String =>
          put {
            handleWith { comment: Comment =>
              listingActor? UpdateComment(listingId, comment.copy(id = Some(commentId)))
            }
          } ~
          delete {
            _ complete listingActor? DeleteComment(listingId, commentId)
          }
        }
      } ~
      pathPrefix("likes") {
        pathEnd {
          get {
            _ complete (listingActor ? GetLikes(listingId)).mapTo[Option[List[Like]]]
          } ~
          post {
            handleWith { comment: Comment =>
              listingActor ? NewComment(listingId, comment)
            }
          }
        } ~
        path(Segment) { likeId =>
          delete {
            _ complete listingActor ? UnLike(listingId, likeId)
          }
        }
      }
    }
  }
}
