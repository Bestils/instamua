package com.htvu.instamua.rest.api.services

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.htvu.instamua.rest.api.JsonFormats
import com.htvu.instamua.rest.dao._
import com.htvu.instamua.rest.util.ActorExecutionContextProvider
import spray.routing.Directives

class ListingService()(implicit system: ActorSystem) extends Directives with JsonFormats {

  import ListingActor._

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


object ListingActor {
  case class NewListing(listing: Listing)
  case class GetListing(listingId: String)
  case class UpdateListing(listingId: String, updated: ListingDetail)
  case class DeleteListing(listingId: String)

  case class GetComments(listingId: String)
  case class NewComment(listingId: String, comment: Comment)
  case class UpdateComment(listingId: String, comment: Comment)
  case class DeleteComment(listingId: String, commentId: String)

  case class GetLikes(listingId: String)
  case class NewLike(listingId: String, like: Like)
  case class UnLike(listingId: String, likeId: String)

  case class SearchListing(query: String)

  def props(): Props = Props(new ListingActor())
}

class ListingActor extends Actor with ListingDAO with ActorExecutionContextProvider{
  import ListingActor._

  def receive: Receive = {
    case NewListing(listing) =>
      createNewListing(listing) pipeTo sender
    case GetListing(listingId) =>
      println(listingId)
      getListing(listingId) pipeTo sender
    case UpdateListing(listingId, updated) =>
      updateListing(listingId, updated) pipeTo sender
    case DeleteListing(listingId) =>
      deleteListing(listingId) pipeTo sender
    case GetComments(listingId) =>
      getComments(listingId) pipeTo sender
    case NewComment(listingId, comment) =>
      createNewComment(listingId, comment) pipeTo sender
    case UpdateComment(listingId, comment) =>
      updateComment(listingId, comment) pipeTo sender
    case DeleteComment(listingId, commentId) =>
      deleteComment(listingId, commentId) pipeTo sender
    case GetLikes(listingId) =>
      getLikes(listingId) pipeTo sender
    case NewLike(listingId, like) =>
      createNewLike(listingId, like) pipeTo sender
    case UnLike(listingId, likeId) =>
      unlike(listingId, likeId) pipeTo sender
    case SearchListing(query) =>
      search(query) pipeTo sender
  }
}



