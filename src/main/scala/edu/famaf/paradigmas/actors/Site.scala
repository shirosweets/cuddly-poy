package edu.famaf.paradigmas

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.Signal
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.LoggerOps
import akka.util.Timeout
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object Site {
  def apply(): Behavior[SiteCommands_Request] =
    Behaviors.setup(context => new Site(context))

  /* Mensajes Site */
  sealed trait SiteCommands_Request
  final case class New_Subcription(url: String, url_Type: String, feeds: List[String])
    extends SiteCommands_Request
  final case class AdaptedResponse(message: String) extends SiteCommands_Request

}

class Site(context: ActorContext[Site.SiteCommands_Request])
    extends AbstractBehavior[Site.SiteCommands_Request](context) {
  context.log.info("Site Started")

  import Site._

  var word = "%s".r

  // Variable para llevar registro de los feeds
  var feed_list :List[((ActorRef[Feed.FeedCommands_Request]), String)] = List()

  override def onMessage(msg: SiteCommands_Request):
    Behavior[SiteCommands_Request] = {
    var urls: List[(String, String, String)] = List()

    msg match {
      case New_Subcription(url, url_Type, feeds) =>
        // Remplazamos los feeds, y guardamos los datos en una tripla.
        implicit val timeout: Timeout = 3.seconds
        feeds.foreach{data => urls =
          (word.replaceFirstIn(url, data), data, url_Type) :: urls}

        // Creamos los actores feeds necesarios y enviamos la información
        // de la url y la url_Type al actor Feed.
        // TODO Change to parse url
        urls.foreach{data => val new_feed =
          context.spawn(Feed(), s"New_Feed_${feed_list.length}:${data._2}")
          context.ask(new_feed, (ref) => Feed.Give_Parse(ref, data)) {
            case Success(Feed.Response_Feed(message)) =>
              AdaptedResponse(message)
            case Failure(_) =>
              AdaptedResponse("Request failed")
          }
          // TODO End change to parse url
          feed_list = (new_feed, data._2) :: feed_list
        }
        Behaviors.same
      case AdaptedResponse(message) =>
        context.log.info("Text Parsed: {}", message)
        Behaviors.same
    }
  }
}