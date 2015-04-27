package com.pjanof.services

import scala.util.Random

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

import spray.routing._
import spray.http._

import MediaTypes._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.lang.Runtime
import java.net.URI

import com.pjanof.actors.RandomActor

class RoutingServiceActor extends Actor with RoutingService {

  /** actor system
   * - connects the services environment to the enclosing actor or test
   */
  def actorRefFactory = context

  // routing
  def receive = runRoute(routes)
}

trait RoutingService extends HttpService {

  /** use the enclosing ActorContext's or ActorSystem's dispatcher for
   * - Futures
   * - Scheduler
   */
  implicit def ec = actorRefFactory.dispatcher

  implicit val timeout = Timeout(5.seconds)

  val routes =

    pathPrefix("examples") {

      /** GET Random Number
       *
       * curl -X GET http://127.0.0.1:8080/examples/random | python -mjson.tool
       */
      path("random") {
        get {
          respondWithMediaType(`application/json`) {
            complete {
              val rand = new Random
              val next: Int = rand.nextInt
              s"""{ "number": $next  }"""
            }
          }
        }
      } ~
      /** GET Random Number from Future
       *
       * curl -X GET http://127.0.0.1:8080/examples/futures | python -mjson.tool
       */
      path("futures") {
        get {
          respondWithMediaType(`application/json`) {
            complete {
              val f: Future[Int] = Future {
                val rand = new Random
                rand.nextInt
              }
              f.map(x => s"""{ "number": $x  }""")
            }
          }
        }
      } ~
      /** GET Random Number from Actor
       *
       * curl -X GET http://127.0.0.1:8080/examples/actors | python -mjson.tool
       */
      path("actors") {
        get {
          respondWithMediaType(`application/json`) {
            complete {
              val actor = actorRefFactory.actorOf(Props(classOf[RandomActor]))
              val f: Future[Int] = ask(actor, "random").mapTo[Int]
              f.map(x => s"""{ "number": $x  }""")
            }
          }
        }
      }
    }
}
