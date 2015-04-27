package com.pjanof.actors

import akka.actor.Actor
import akka.actor.ActorLogging

import scala.util.Random

class RandomActor() extends Actor with ActorLogging {

  def receive = {

    case "random" =>
      val rand = new Random
      sender ! rand.nextInt
  }
}
