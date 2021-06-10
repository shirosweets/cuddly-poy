package edu.famaf.paradigmas

import akka.actor.typed.Signal
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.LoggerOps
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior

object RequestParse {
  def apply(): Behavior[FeedCommands_Request] =
    Behaviors.setup(context => new RequestParse(context))

  /* Mensajes de RequestParse */
  sealed trait FeedCommands_Request
  final case class GiveToParse(
    url: String,
    url_Type: String,
    supervisor_ref: ActorRef[Supervisor.SupervisorCommand])
      extends FeedCommands_Request
}

class RequestParse(context: ActorContext[RequestParse.FeedCommands_Request])
    extends AbstractBehavior[RequestParse.FeedCommands_Request](context) {
  context.log.info("RequestParse Started")

  import RequestParse._

  override def onMessage(msg: FeedCommands_Request):
      Behavior[FeedCommands_Request] = {
    msg match {
      // Creamos una tupla donde guardaremos la informacion de la url.
      case GiveToParse(url, url_Type, supervisor_ref) =>
        var feed: String = ""
        url_Type match {
          case "rss" => val url_to_parse = new RSS_Parse
            feed = url_to_parse.parser(url).mkString(" ")
          case "reddit" => val url_to_parse = new REDDIT_Parse
            feed = url_to_parse.parser(url).mkString(" ")
        }
        supervisor_ref ! Supervisor.SendFeed(feed)
        Behaviors.same
    }
  }
}