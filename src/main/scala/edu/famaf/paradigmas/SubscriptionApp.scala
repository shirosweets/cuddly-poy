package edu.famaf.paradigmas

import akka.actor.typed.ActorSystem
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.slf4j.{Logger, LoggerFactory}
import scala.io._
import scopt.OParser

object SubscriptionApp extends App {
  implicit val formats = DefaultFormats

  val logger: Logger = LoggerFactory.getLogger("edu.famaf.paradigmas.SubscriptionApp")

  case class Subscription(name: String, feeds: List[String], url: String)

  case class Config(
    input: String = "",
    maxUptime: Int = 10
  )

  private def readSubscriptions(filename: String): List[Subscription] = {
    println(s"Reading subscriptions from ${filename}")
    val jsonContent = Source.fromFile(filename)
    (parse(jsonContent.mkString)).extract[List[Subscription]]
  }

  val builder = OParser.builder[Config]
  val argsParser = {
    import builder._
    OParser.sequence(
      programName("akka-subscription-app"),
      head("akka-subscription-app", "1.0"),
      opt[String]('i', "input")
        .action((input, config) => config.copy(input = input))
        .text("Path to json input file"),
      opt[Int]('t', "max-uptime")
        .optional()
        .action((uptime, config) => config.copy(maxUptime = uptime))
        .text("Time in seconds before sending stop signal"),
    )
  }

  OParser.parse(argsParser, args, Config()) match {
    case Some(config) =>
      val system = ActorSystem[Supervisor.SupervisorCommand](Supervisor(), "subscription-app")
      val readSubs = readSubscriptions(config.input)
      readSubs.foreach{x => system ! Supervisor.JsonSubs(x.name,x.feeds,x.url)}
      Thread.sleep(config.maxUptime * 1000)
      system ! Supervisor.Stop()
    case _ => ???
  }

}
