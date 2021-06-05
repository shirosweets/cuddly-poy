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
  final case class Get_Feed_Parsed() extends FeedCommands_Request

  sealed trait FeedCommands_Response
  final case class Send_Feed_Parsed() extends FeedCommands_Response
}

class Feed(context: ActorContext[Feed.FeedCommands_Request])
    extends AbstractBehavior[Feed.FeedCommands_Request](context) {
  context.log.info("Feed Started")

  import Feed._

  override def onMessage(msg: FeedCommands_Request): Behavior[FeedCommands_Request] = {
    msg match {
      case Get_Feed_Parsed() =>
        Behaviors.same
    }
  }
}