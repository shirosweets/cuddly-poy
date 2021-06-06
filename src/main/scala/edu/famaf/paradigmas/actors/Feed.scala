package edu.famaf.paradigmas

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.Signal
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.LoggerOps

object Feed {
  def apply(): Behavior[FeedCommands_Request] =
    Behaviors.setup(context => new Feed(context))

  /* Mensajes de Feed */
  sealed trait FeedCommands_Request
  final case class Give_Parse(replyTo: ActorRef[Response_Feed], data: (String, String, String)) extends FeedCommands_Request

  sealed trait FeedCommands_Response
  final case class Response_Feed(message: String) extends FeedCommands_Response
}

class Feed(context: ActorContext[Feed.FeedCommands_Request])
    extends AbstractBehavior[Feed.FeedCommands_Request](context) {
  context.log.info("Feed Started")

  import Feed._

  override def onMessage(msg: FeedCommands_Request): Behavior[FeedCommands_Request] = {
    msg match {
      // Creamos una tupla donde guardaremos la informacion de la url.
      case Give_Parse(replyTo, data) =>
        var feed: String = ""
        //
        data._3 match {
          case "rss" => val url_to_parse = new RSS_Parse
            feed = url_to_parse.parser(data._1).mkString(" ")
          case "reddit" => val url_to_parse = new REDDIT_Parse
            feed = url_to_parse.parser(data._1).mkString(" ")
        }
        replyTo ! Response_Feed(feed)
        Behaviors.same
    }
  }
}