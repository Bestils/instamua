package com.htvu.instamua.rest.dao

import reactivemongo.bson.{BSONObjectID, BSONDocument}
import reactivemongo.core.commands.LastError

import scala.concurrent.{ExecutionContext, Future}

trait ListingDAO extends MongoConnector {
  implicit def executionContext: ExecutionContext

  def getListing(listingId: String): Future[Option[Listing]] =
    listings.find(BSONDocument("_id" -> BSONObjectID(listingId))).one[Listing]

  def createNewListing(listing: Listing): Future[Any] =
    listings.insert[Listing](listing.copy(threadId = Some(BSONObjectID.generate)))

  def updateListing(listingId: String, updated: ListingDetail): Future[Any] =
    listings.update(BSONDocument("_id" -> BSONObjectID(listingId)), BSONDocument("$set" -> BSONDocument("details" -> updated)))

  def deleteListing(listingId: String): Future[Any] =
    listings.remove(BSONDocument("_id" -> BSONObjectID(listingId)), firstMatchOnly = true)

  def getComments(threadId: String): Future[List[Comment]] =
    comments.find(BSONDocument("threadId" -> BSONObjectID(threadId))).cursor[Comment].collect[List]()

  def createNewComment(comment: Comment): Future[LastError] = comments.insert[Comment](comment)

  def updateComment(comment: Comment): Future[LastError] =
    comments.update(BSONDocument("_id" -> comment.threadId), BSONDocument("$set" -> BSONDocument("text" -> comment.text)))

  def deleteComment(commentId: String): Future[LastError] =
    comments.remove(BSONDocument("_id" -> BSONObjectID(commentId)))

  def getLikes(listingId: String): Future[Option[List[Like]]] =
    listings.find(BSONDocument("_id" -> BSONObjectID(listingId)), BSONDocument("likes" -> 1)).one[LikeProjection] map (l => l.map(_.likes))

  def createNewLike(listingId: String, like: Like): Future[Any] = {
    listings.update(BSONDocument("_id" -> BSONObjectID(listingId)), BSONDocument("$push" -> BSONDocument("likes" -> like)))
  }

  def unlike(listingId: String, likeId: String): Future[Any] =
    listings.update(BSONDocument("_id" -> BSONObjectID(listingId)), BSONDocument("$pull" -> BSONDocument("likes" -> BSONDocument("id" -> likeId))))

  def search(query: String): Future[List[Listing]] =
    listings.find(BSONDocument("$text" -> BSONDocument("$search" -> query))).cursor[Listing].collect[List]()
}


