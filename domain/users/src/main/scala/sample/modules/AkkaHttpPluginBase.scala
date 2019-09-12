package sample.modules

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import izumi.distage.model.definition.ModuleDef

import scala.concurrent.ExecutionContext

class AkkaHttpPluginBase extends ModuleDef {
  make[ActorSystem].from {
    ActorSystem("distage-sample")
  }

  make[Materializer].from {
    system: ActorSystem =>
      ActorMaterializer()(system)
  }

  make[ExecutionContext].named("akka-ec").from {
    system: ActorSystem =>
      system.dispatcher
   }
}
