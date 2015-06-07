package com.htvu.instamua.rest.dao

import reactivemongo.bson.{BSONObjectID, BSONDocument}

import scala.concurrent.{ExecutionContext, Future}

trait ListingDAO extends MongoConnector {
  implicit def executionContext: ExecutionContext

  def getListing(listingId: String): Future[Option[Listing]] =
    listings.find(BSONDocument("_id" -> BSONObjectID(listingId))).one[Listing]

  def createNewListing(listing: Listing): Future[Any] =
    listings.insert[Listing](listing)

  def updateListing(listingId: String, updated: ListingDetail): Future[Any] =
    listings.update(BSONDocument("_id" -> BSONObjectID(listingId)), BSONDocument("$set" -> BSONDocument("details" -> updated)))

  def deleteListing(listingId: String): Future[Any] =
    listings.remove(BSONDocument("_id" -> BSONObjectID(listingId)), firstMatchOnly = true)


  def getComments(listingId: String): Future[Option[List[Comment]]] =
    listings.find(BSONDocument("_id" -> BSONObjectID(listingId)), BSONDocument("comments" -> 1)).one[CommentProjection] map (l => l.map(_.comments))

  def createNewComment(listingId: String, comment: Comment): Future[String] = {
    val added = comment.copy(id = Some(BSONObjectID.generate.stringify))
    listings.update(BSONDocument("_id" -> BSONObjectID(listingId)), BSONDocument("$push" -> BSONDocument("comments" -> added))) map (_ => added.id.get)
  }

  def updateComment(listingId: String, comment: Comment): Future[Any] =
    listings.update(BSONDocument("_id" -> BSONObjectID(listingId), "comments.id" -> comment.id.get), BSONDocument("$set" -> BSONDocument("comments.$" -> comment)))

  def deleteComment(listingId: String, commentId: String): Future[Any] =
    listings.update(BSONDocument("_id" -> BSONObjectID(listingId)), BSONDocument("$pull" -> BSONDocument("comments" -> BSONDocument("id" -> commentId))))


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


