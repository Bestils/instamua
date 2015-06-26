package com.htvu.instamua.rest.api.services

import akka.actor.{Props, Actor, ActorSystem}
import akka.util.Timeout
import com.htvu.instamua.rest.api.JsonFormats
import com.htvu.instamua.rest.dao.{ListingDAO, Comment}
import com.htvu.instamua.rest.util.ActorExecutionContextProvider
import spray.routing.Directives
import akka.pattern.{ pipe, ask}

class CommentService()(implicit system: ActorSystem) extends Directives with JsonFormats{

  val commentActor = system.actorOf(CommentActor.props(), "comment-actor")
  implicit val ec = system.dispatcher
  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)

  import CommentActor._

  val routes =  pathPrefix("comments") {
    path(Segment) { id: String => //id could be threadId(get) or commentId(delete)
      get {
        _ complete (commentActor? GetComments(id)).mapTo[List[Comment]]
      } ~
      delete {
        _ complete commentActor? DeleteComment(id)
      }
    } ~
    pathEnd {
      post {
        handleWith { comment: Comment =>
          (commentActor? NewComment(comment)).mapTo[Boolean]
        }
      } ~
      put {
        handleWith { comment: Comment =>
          commentActor? UpdateComment(comment)
        }
      }
    }
  }
}

object CommentActor {
  case class GetComments(threadId: String)
  case class NewComment(comment: Comment)
  case class UpdateComment(comment: Comment)
  case class DeleteComment(commentId: String)

  def props(): Props = Props(new CommentActor)
}

class CommentActor() extends Actor with ListingDAO with ActorExecutionContextProvider {
  import CommentActor._

  def receive: Receive = {
    case GetComments(threadId) =>
      getComments(threadId) pipeTo sender
    case NewComment(comment) =>
      createNewComment(comment) pipeTo sender
    case UpdateComment(comment) =>
      updateComment(comment) pipeTo sender
    case DeleteComment(commentId) =>
      deleteComment(commentId) pipeTo sender
  }
}
