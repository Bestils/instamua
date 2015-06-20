package com.htvu.instamua.rest.util

import akka.actor._

//nothing special
trait ActorRefFactoryProvider {
  implicit def actorRefFactory: ActorRefFactory

  def actorSystem(implicit refFactory: ActorRefFactory): ExtendedActorSystem = spray.util.actorSystem(refFactory)
}

//just to provide implicit actorFactory by extending it from the Actor subclass
trait ActorRefFactoryProviderForActors extends ActorRefFactoryProvider {
  this: Actor ⇒
  def actorRefFactory = context
}
