package com.htvu.instamua.rest.api

import reactivemongo.core.commands.LastError

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

package object services {

  implicit class FutureOps[U](f: Future[U]) {
    def toResponse[T](implicit tag: ClassTag[T], ec: ExecutionContext): Future[BaseResponse[T]] = toBaseResponse(f.mapTo[T])

    private def toBaseResponse[T](response: Future[T])(implicit ec: ExecutionContext): Future[BaseResponse[T]] = {
      val p = Promise[BaseResponse[T]]()
      response onComplete {
        case Success(t) => t match {
          case e: LastError =>
            if (e.ok) p success BaseResponse[T](success = true, 200, Nil, None)
            else p success BaseResponse[T](success = false, 200, List(e.message), None)
          case _ =>
            p success BaseResponse[T](true, 200, Nil, Some(t))
        }
        case Failure(e) =>
          p success new BaseResponse[T](false, 502, List(e.getMessage), None)
      }
      p.future
    }
  }

  case class BaseResponse[T](success: Boolean, errorCode: Int, errorMessages: List[String], data: Option[T])

}
