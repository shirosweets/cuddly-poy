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
  final case class New_Url(url: String, url_Type: String) extends FeedCommands_Request
  final case class Get_Feed_Parsed() extends FeedCommands_Request

  sealed trait FeedCommands_Response
  final case class Send_Feed_Parsed() extends FeedCommands_Response
}

class Feed(context: ActorContext[Feed.FeedCommands_Request])
    extends AbstractBehavior[Feed.FeedCommands_Request](context) {
  context.log.info("Feed Started")

  import Feed._

  // Variable para guardar la url y el tipo.
  var url_t: (String, String) = ("", "")
  override def onMessage(msg: FeedCommands_Request): Behavior[FeedCommands_Request] = {
    msg match {
      // Creamos una tupla donde guardaremos la informacion de la url.
      case New_Url(url, url_Type) => url_t = (url, url_Type)
        //println(s"Mi url:${url_t}")
        Behaviors.same
      case Get_Feed_Parsed() =>
        var feed: String = ""

        // Dependiendo el tipo de url, creamos un parse.
        url_t._2 match {
          case "rss" => val url_to_parse = new RSS_Parse
            feed = url_to_parse.parser(url_t._1).mkString(" ")
          case "reddit" => val url_to_parse = new REDDIT_Parse
            feed = url_to_parse.parser(url_t._1).mkString(" ")
        }
        context.log.info(feed)
        Behaviors.same
    }
  }
}