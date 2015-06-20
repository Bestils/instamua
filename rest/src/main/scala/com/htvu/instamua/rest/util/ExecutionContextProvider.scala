package com.htvu.instamua.rest.util

import scala.concurrent.ExecutionContext
import akka.actor._

//this is not going to be used directly
trait ExecutionContextProvider {
  implicit def executionContext: ExecutionContext
}

//allow getting implicit execution context from the actorSystemFactory
trait ActorRefFactoryExecutionContextProvider extends ExecutionContextProvider with ActorRefFactoryProvider {
  implicit def executionContext = actorRefFactory.dispatcher
}

//to getting implicit execution context by extend it in the Actor subclass
trait ActorExecutionContextProvider extends ExecutionContextProvider {
  this: Actor ⇒
  implicit def executionContext = context.dispatcher
}

