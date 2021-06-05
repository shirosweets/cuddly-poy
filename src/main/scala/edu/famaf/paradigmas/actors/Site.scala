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
  final case class Site_Handle(name: String) extends SiteCommands_Request
  final case class Get_Feed() extends SiteCommands_Request
  final case class Get_All_Feeds() extends SiteCommands_Request

  sealed trait SiteCommands_Response
  final case class Send_Feed() extends SiteCommands_Response
  final case class Send_All_Feeds() extends SiteCommands_Response
}

class Site(context: ActorContext[Site.SiteCommands_Request])
    extends AbstractBehavior[Site.SiteCommands_Request](context) {
  context.log.info("Site Started")

  import Site._
  var feed_list :List[ActorRef[Feed.FeedCommands_Request]] = List()
  override def onMessage(msg: SiteCommands_Request): Behavior[SiteCommands_Request] = {
    msg match {
      case Site_Handle(name) => println(s"This is Site_Handle ${name}")
        val new_feed = context.spawn(Feed(),s"New_Feed:${name}")
        feed_list = new_feed :: feed_list
        Behaviors.same
    }
  }
}