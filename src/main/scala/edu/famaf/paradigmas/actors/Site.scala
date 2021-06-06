package edu.famaf.paradigmas

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.Signal
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.LoggerOps

object Site {
  def apply(): Behavior[SiteCommands_Request] =
    Behaviors.setup(context => new Site(context))

  /* Mensajes Site */
  sealed trait SiteCommands_Request
  final case class New_Subcription(url: String, url_Type: String, feeds: List[String])
    extends SiteCommands_Request
  final case class Get_Feed(feed: String) extends SiteCommands_Request
  final case class Get_All_Feeds() extends SiteCommands_Request

  sealed trait SiteCommands_Response
  final case class Send_Feed() extends SiteCommands_Response
  final case class Send_All_Feeds() extends SiteCommands_Response
}

class Site(context: ActorContext[Site.SiteCommands_Request])
    extends AbstractBehavior[Site.SiteCommands_Request](context) {
  context.log.info("Site Started")

  import Site._

  var word = "%s".r

  // Variable para llevar registro de los feeds
  var feed_list :List[((ActorRef[Feed.FeedCommands_Request]), String)] = List()

  override def onMessage(
    msg: SiteCommands_Request): Behavior[SiteCommands_Request] = {
    var urls: List[(String,String,String)] = List()

    msg match {
      case New_Subcription(url,url_Type,feeds) =>
        // Remplazamos los feeds, y guardamos los datos en una tripla.
        feeds.foreach{feed => urls =
          (word.replaceFirstIn(url,feed), feed, url_Type) :: urls}

        // Creamos los actores feeds necesarios y enviamos la información
        // de la url y la url_Type al actor Feed.
        urls.foreach{feed => val new_feed =
          context.spawn(Feed(), s"New_Feed_${feed_list.length}:${feed._2}")
          new_feed ! Feed.New_Url(feed._1,feed._3)
          feed_list = (new_feed, feed._2) :: feed_list}
        Behaviors.same
      case Get_Feed(feed) =>
        // Buscamos el feed que pide y enviamos el pedido al actor Feed.
        feed_list.foreach{to_parse => if(to_parse._2 == feed)
          { to_parse._1 ! Feed.Get_Feed_Parsed()}}
        Behaviors.same
      case Get_All_Feeds() =>
        // Pedimos a todos los actores, el feed.
        feed_list.foreach{to_parse => to_parse._1 ! Feed.Get_Feed_Parsed()}
        Behaviors.same
    }
  }
}