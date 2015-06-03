package com.htvu.instamua.user.dao

import scala.concurrent.Future

object ListingDAO {
  def getListing(listingId: String): Future[Option[Listing]] = ???

  def createNewListing(listing: Listing): Future[Listing] = ???

  def updateListing(listingId: String, listing: Listing): Future[Any] = ???

  def deleteListing(listingId: String): Future[Any] = ???


  def getComments(listingId: String): Future[Seq[Comment]] = ???

  def createNewComment(listingId: String, comment: Comment): Future[Comment] = ???

  def updateComment(listingId: String, comment: Comment): Future[Any] = ???

  def deleteComment(listingId: String, commentId: String): Future[Any] = ???


  def getLikes(listingId: String): Future[Seq[Like]] = ???

  def createNewLike(listingId: String, like: Like): Future[Any] = ???

  def unlike(listingId: String, likeId: Long): Future[Any] = ???

  def search(query: String): Future[Seq[Listing]] = ???
}
