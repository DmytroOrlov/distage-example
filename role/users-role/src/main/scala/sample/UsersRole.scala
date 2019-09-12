package sample

import izumi.distage.model.definition.DIResource
import izumi.distage.roles.model.{RoleDescriptor, RoleService}
import izumi.fundamentals.platform.cli.model.raw.RawEntrypointParams
import izumi.fundamentals.platform.language.Quirks._
import sample.http.HttpServerLauncher
import logstage.LogBIO

class UsersRole[F[+ _, + _]]
(
  http: HttpServerLauncher.StartedServer
, log: LogBIO[F]
) extends RoleService[F[Throwable, ?]] {

  // force resource start-up
  http.discard()

  override def start(roleParameters: RawEntrypointParams, freeArgs: Vector[String]): DIResource[F[Throwable, ?], Unit] = {
    DIResource.make(
      acquire = log.info("Entrypoint reached: users role")
    )(release = _ =>
      log.info("Exit reached: users role")
    )
  }
}

object UsersRole extends RoleDescriptor {
  override final val id = "users"
}
