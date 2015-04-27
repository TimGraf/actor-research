package com.pjanof

import spray.can.Http

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import java.io.File

import services._

object Boot extends App {

  val prop: String = System.getProperty("config.file")

  val configFile: String = if ( prop == null ) { "src/main/resources/application.conf" } else { prop }

  val config: Config = ConfigFactory.load(ConfigFactory.parseFileAnySyntax(new File(configFile)))

  implicit val system = ActorSystem("pjanof-actor-research", config)

  val service = system.actorOf(Props[RoutingServiceActor], "actor-research-service")

  implicit val timeout = Timeout(5.seconds)

  IO(Http) ? Http.Bind(service, interface = config.getString("service.host"), port = config.getInt("service.port"))
}
