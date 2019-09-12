package sample.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.stream.Materializer
import izumi.distage.config.annotations.ConfPath
import izumi.distage.model.definition.{DIResource, Id}
import izumi.functional.bio.BIO
import izumi.functional.bio.BIO._
import logstage.LogBIO
import sample.http.HttpServerLauncher.{HttpConfig, StartedServer}
import sample.plugins.BIOFromFuture

import scala.concurrent.{ExecutionContext, Future}

final class HttpServerLauncher[F[+_, +_]: BIO: BIOFromFuture]
(
  routes: Set[RouterSet[F]]
, httpCfg: HttpConfig @ConfPath("http")
, log: LogBIO[F]
)(implicit
  as: ActorSystem
, mat: Materializer
, ec: ExecutionContext @Id("akka-ec")
) extends DIResource[F[Throwable, ?], HttpServerLauncher.StartedServer] {

  override def acquire: F[Throwable, StartedServer] = {
    val router = routes.toList.map(_.akkaRouter) match {
      case Nil => RouteDirectives.reject
      case head :: tail => tail.fold(head)(_ ~ _)
    }
    for {
      _ <- log.info(s"Starting HTTP server with $httpCfg")
      server <- BIO {
        StartedServer(Http().bindAndHandle(router, httpCfg.host, httpCfg.port))
      }
    } yield server
  }

  override def release(resource: StartedServer): F[Throwable, Unit] = {
    log.info(s"Shutting down HTTP server") *>
    BIOFromFuture[F].fromFuture {
      BIO {
        resource.serverFuture
          .flatMap(_.unbind()) // trigger unbinding from the port
          .flatMap(_ => as.terminate()) // and shutdown when done
          .map(_ => ())
      }
    }
  }

}

object HttpServerLauncher {
  final case class StartedServer(private[HttpServerLauncher] val serverFuture: Future[ServerBinding])

  final case class HttpConfig(host: String, port: Int)
}
