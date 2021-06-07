package edu.famaf.paradigmas

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.Signal
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.LoggerOps

object Request_Parse {
  def apply(): Behavior[FeedCommands_Request] =
    Behaviors.setup(context => new Request_Parse(context))

  /* Mensajes de Request_Parse */
  sealed trait FeedCommands_Request
  final case class Give_Parse(replyTo: ActorRef[Response_Feed], url: String, url_Type: String) 
    extends FeedCommands_Request

  sealed trait FeedCommands_Response
  final case class Response_Feed(message: String) extends FeedCommands_Response
}

class Request_Parse(context: ActorContext[Request_Parse.FeedCommands_Request])
    extends AbstractBehavior[Request_Parse.FeedCommands_Request](context) {
  context.log.info("Request_Parse Started")

  import Request_Parse._

  override def onMessage(msg: FeedCommands_Request): Behavior[FeedCommands_Request] = {
    msg match {
      // Creamos una tupla donde guardaremos la informacion de la url.
      case Give_Parse(replyTo, url, url_Type) =>
        var feed: String = ""
        //
        url_Type match {
          case "rss" => val url_to_parse = new RSS_Parse
            feed = url_to_parse.parser(url).mkString(" ")
          case "reddit" => val url_to_parse = new REDDIT_Parse
            feed = url_to_parse.parser(url).mkString(" ")
        }
        replyTo ! Response_Feed(feed)
        Behaviors.same
    }
  }
}