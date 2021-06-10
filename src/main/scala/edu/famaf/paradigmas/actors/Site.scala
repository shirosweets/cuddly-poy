package edu.famaf.paradigmas

import akka.actor.typed.Signal
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.LoggerOps
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior

object Site {
  def apply(): Behavior[SiteCommands_Request] =
    Behaviors.setup(context => new Site(context))

  /* Mensajes Site */
  sealed trait SiteCommands_Request
  final case class GetFeeds(
    url: String,
    url_Type: String,
    feeds: List[String],
    supervisor_ref: ActorRef[Supervisor.SupervisorCommand])
    extends SiteCommands_Request

}

class Site(context: ActorContext[Site.SiteCommands_Request])
    extends AbstractBehavior[Site.SiteCommands_Request](context) {
  context.log.info("Site Started")

  import Site._

  var word = "%s".r

  // Variable para llevar registro de los feeds
  var feed_list :List[(ActorRef[RequestParse.FeedCommands_Request])] = List()

  override def onMessage(msg: SiteCommands_Request):
    Behavior[SiteCommands_Request] = {
    var urls: List[(String, String, String)] = List()

    msg match {
      case GetFeeds(url, url_Type, feeds, supervisor_ref) =>
        // Remplazamos los feeds, y guardamos los datos en una tripla.
        if (feeds != Nil){
          feeds.foreach{data =>
            urls = (word.replaceFirstIn(url, data), data, url_Type) :: urls}
        }
        else{urls = (url, "", url_Type) :: urls}
        // Creamos los actores feeds necesarios y enviamos la información
        // de la url y la url_Type al actor RequestParse.
        urls.foreach{data => val new_RequestParse =
          context.spawn(RequestParse(),
            s"New_RequestParse:_${feed_list.length}:${data._2}")
          new_RequestParse ! RequestParse.GiveToParse(
            data._1, data._3, supervisor_ref)
          feed_list = new_RequestParse :: feed_list
        }
        Behaviors.same
    }
  }
}