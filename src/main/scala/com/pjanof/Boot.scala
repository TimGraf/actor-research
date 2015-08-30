package com.pjanof

import spray.can.Http

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

import java.io.File

import services._

object Boot extends App with StrictLogging {

  val prop: String = System.getProperty("config.file")

  val configFile: String = if ( prop == null ) { "src/main/resources/application.conf" } else { prop }

  val config: Config = ConfigFactory.load(ConfigFactory.parseFileAnySyntax(new File(configFile)))

  implicit val system = ActorSystem("pjanof-actor-research", config)

  system.registerOnTermination { logger.info("Actor System Terminating") }

  val service = system.actorOf(Props[RoutingServiceActor], "actor-research-service")

  implicit val timeout = Timeout(config.getInt("service.actor.timeout-in-seconds") seconds)

  IO(Http) ? Http.Bind(service, interface = config.getString("service.host"), port = config.getInt("service.port"))
}
