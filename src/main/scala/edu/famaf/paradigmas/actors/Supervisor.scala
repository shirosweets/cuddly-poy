package edu.famaf.paradigmas

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.Signal
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.LoggerOps

object Supervisor {
  def apply(): Behavior[SupervisorCommand] =
    Behaviors.setup(context => new Supervisor(context))

  /* Mensajes Supervisor*/
  sealed trait SupervisorCommand
  final case class JsonSubs(name: String, feeds: List[String], url: String, url_Type: String)
    extends SupervisorCommand
  final case class Stop() extends SupervisorCommand
}

class Supervisor(context: ActorContext[Supervisor.SupervisorCommand])
    extends AbstractBehavior[Supervisor.SupervisorCommand](context) {
  context.log.info("Supervisor Started")

  import Supervisor._

  // Variable para llevar informacion de las subscripciones
  var site_list :List[ActorRef[Site.SiteCommands_Request]] = List()

  override def onMessage(msg: SupervisorCommand): Behavior[SupervisorCommand] = {
    msg match {
      case JsonSubs(name, feeds, url, url_Type) =>
        val new_site = context.spawn(Site(), s"New_Sub_${site_list.length}:${name}")

        // Para llevar la cuenta de los sitios que me suscribi
        site_list = new_site :: site_list

        //Envio la informacion necesaria para la subscripcion
        new_site ! Site.GetFeeds(url, url_Type, feeds)
        new_site ! Site.GetFeed(url, url_Type, feeds.head)

        Behaviors.same
      case Stop() => Behaviors.stopped
    }
  }

  override def onSignal: PartialFunction[Signal, Behavior[SupervisorCommand]] = {
    case PostStop =>
      context.log.info("Supervisor Stopped")
      this
  }
}