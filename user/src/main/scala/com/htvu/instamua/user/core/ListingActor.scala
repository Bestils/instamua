package com.htvu.instamua.user.core

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import com.htvu.instamua.user.dao._


object ListingActor {
  case class NewListing(listing: Listing)
  case class GetListing(listingId: String)
  case class UpdateListing(listingId: String, listing: Listing)
  case class DeleteListing(listingId: String)

  case class GetComments(listingId: String)
  case class NewComment(listingId: String, comment: Comment)
  case class UpdateComment(listingId: String, comment: Comment)
  case class DeleteComment(listingId: String, commentId: String)

  case class GetLikes(listingId: String)
  case class NewLike(listingId: String, like: Like)
  case class UnLike(listingId: String, likeId: Long)

  case class SearchListing(query: String)

  def props(): Props = Props(new UserActor())
}

class ListingActor extends Actor {
  implicit val exec = context.dispatcher

  import ListingActor._

  def receive: Receive = {
    case NewListing(listing) =>
      ListingDAO.createNewListing(listing) pipeTo sender
    case GetListing(listingId) =>
      ListingDAO.getListing(listingId) pipeTo sender
    case UpdateListing(listingId, listing) =>
      ListingDAO.updateListing(listingId, listing) pipeTo sender
    case DeleteListing(listingId) =>
      ListingDAO.deleteListing(listingId) pipeTo sender
    case GetComments(listingId) =>
      ListingDAO.getComments(listingId) pipeTo sender
    case NewComment(listingId, comment) =>
      ListingDAO.createNewComment(listingId, comment) pipeTo sender
    case UpdateComment(listingId, comment) =>
      ListingDAO.updateComment(listingId, comment) pipeTo sender
    case DeleteComment(listingId, commentId) =>
      ListingDAO.deleteComment(listingId, commentId) pipeTo sender
    case GetLikes(listingId) =>
      ListingDAO.getLikes(listingId) pipeTo sender
    case NewLike(listingId, like) =>
      ListingDAO.createNewLike(listingId, like) pipeTo sender
    case UnLike(listingId, likeId) =>
      ListingDAO.unlike(listingId, likeId) pipeTo sender
    case SearchListing(query) =>
      ListingDAO.search(query) pipeTo sender
  }
}



