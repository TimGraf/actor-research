package com.pjanof.services

import scala.util.Random

import akka.actor.Actor
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

import spray.routing._
import spray.http._

import MediaTypes._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.lang.Runtime
import java.io.File
import java.net.URI

import com.pjanof.actors.{InstrumentedActor, RandomActor}

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

class RoutingServiceActor extends InstrumentedActor with RoutingService {

  /** actor system
   * - connects the services environment to the enclosing actor or test
   */
  def actorRefFactory = context

  // routing
  def receive = runRoute(routes)
}

trait RoutingService extends HttpService with StrictLogging {

  /** use the enclosing ActorContext's or ActorSystem's dispatcher for
   * - Futures
   * - Scheduler
   */
  implicit def ec = actorRefFactory.dispatcher

  /** configuration
    * - abstract into ReaderMonad
    */
  val prop: String = System.getProperty("config.file")
  val configFile: String = if ( prop == null ) { "src/main/resources/application.conf" } else { prop }
  val config: Config = ConfigFactory.load(ConfigFactory.parseFileAnySyntax(new File(configFile)))

  implicit val timeout = Timeout(config.getInt("service.actor.timeout-in-seconds") seconds)

  val routes = regularRoute ~ futureRoute ~ randActorRoute ~ timeoutWithRecoverActorRoute ~ timeoutWithoutRecoverActorRoute

  /** GET Random Number
   *
   * curl -X GET http://127.0.0.1:8080/examples/random | python -mjson.tool
   */
  def regularRoute = get {
    path("examples" / "random") {
      respondWithMediaType(`application/json`) {
        complete {
          logger.info("GET examples/random")

          val rand = new Random
          val next: Int = rand.nextInt

          s"""{ "number": $next  }"""
        }
      }
    }
  }

  /** GET Random Number from Future
   *
   * curl -X GET http://127.0.0.1:8080/examples/futures | python -mjson.tool
   */
  def futureRoute = get {
    path("examples" / "futures") {
      respondWithMediaType(`application/json`) {
        complete {
          logger.info("GET examples/futures")

          val f: Future[Int] = Future {
            val rand = new Random
            rand.nextInt
          }

          f.map(x => s"""{ "number": $x  }""")
        }
      }
    }
  }

  /** GET Random Number from Actor
   *
   * curl -X GET http://127.0.0.1:8080/examples/actors/rand/ask | python -mjson.tool
   */
  def randActorRoute = get {
    path("examples" / "actors" / "rand" / "ask") {
      respondWithMediaType(`application/json`) {
        complete {
          logger.info("GET examples/actors/rand/ask")

          val actor = actorRefFactory.actorOf(Props(classOf[RandomActor]))
          val f: Future[Int] = ask(actor, RandomActor.NextRandom()).mapTo[Int]

          f.map(x => s"""{ "number": $x  }""")
        }
      }
    }
  }

  /** GET Timeout Exception from Actor with Recover
   *
   * curl -X GET http://127.0.0.1:8080/examples/actors/timeout/ask/recover | python -mjson.tool
   */
  def timeoutWithRecoverActorRoute = get {
    path("examples" / "actors" / "timeout" / "ask" / "recover") {
      respondWithMediaType(`application/json`) {
        complete {
          logger.info("GET examples/actors/timeout/ask/recover")

          val actor = actorRefFactory.actorOf(Props(classOf[RandomActor]))
          val f: Future[Int] = ask(actor, RandomActor.ForceTimeout(config.getInt("service.actor.timeout-in-seconds") + 5)).mapTo[Int]

          f.map(x => s"""{ "number": $x  }""") recover { case e => s"""{ "exception": "$e" }""" }
        }
      }
    }
  }

  /** GET Timeout Exception from Actor without Recover
   *
   * curl -X GET http://127.0.0.1:8080/examples/actors/timeout/ask/fail | python -mjson.tool
   */
  def timeoutWithoutRecoverActorRoute = get {
    path("examples" / "actors" / "timeout" / "ask" / "fail") {
      respondWithMediaType(`application/json`) {
        complete {
          logger.info("GET examples/actors/timeout/ask/fail")

          val actor = actorRefFactory.actorOf(Props(classOf[RandomActor]))
          val f: Future[Int] = ask(actor, RandomActor.ForceTimeout(config.getInt("service.actor.timeout-in-seconds") + 5)).mapTo[Int]

          f.map(x => s"""{ "number": $x  }""")
        }
      }
    }
  }
}
