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
  final case class JsonSubs(name: String, feeds: List[String], url: String)
    extends SupervisorCommand
  final case class Stop() extends SupervisorCommand
}

class Supervisor(context: ActorContext[Supervisor.SupervisorCommand])
    extends AbstractBehavior[Supervisor.SupervisorCommand](context) {
  context.log.info("Supervisor Started")

  import Supervisor._

  var site_list :List[ActorRef[Site.SiteCommands_Request]] = List()

  override def onMessage(msg: SupervisorCommand): Behavior[SupervisorCommand] = {
    msg match {
      case JsonSubs(name,feeds,url) => println(name,feeds,url)
        val new_site = context.spawn(Site(),s"New_Site${site_list.length}")
        site_list = new_site :: site_list
        new_site ! Site.Site_Handle(name)
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