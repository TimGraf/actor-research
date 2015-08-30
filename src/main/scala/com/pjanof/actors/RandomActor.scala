package com.pjanof.actors

import akka.actor.Actor
import akka.actor.ActorLogging

import scala.util.Random

object RandomActor {
  case class NextRandom()
  case class ForceTimeout(seconds: Int)
}

class RandomActor() extends InstrumentedActor with ActorLogging {

  def receive = {

    case (next: RandomActor.NextRandom) =>
      val rand = new Random
      sender ! rand.nextInt

    case (timeout: RandomActor.ForceTimeout) =>
      Thread.sleep(timeout.seconds)
  }
}
