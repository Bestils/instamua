package com.htvu.instamua.rest.core

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import com.htvu.instamua.rest.dao._

import scala.concurrent.ExecutionContext


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

class ListingActor extends Actor with ListingDAO {
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

  implicit def executionContext: ExecutionContext = context.dispatcher
}



