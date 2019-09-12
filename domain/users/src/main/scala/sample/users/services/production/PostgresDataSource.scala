package sample.users.services.production

import cats.effect.{Async, Blocker, ContextShift, Resource}
import izumi.distage.config.annotations.ConfPath
import distage.Id
import doobie.hikari.HikariTransactor

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object PostgresDataSource {

  def resource[F[_]]
    (cfg: PostgresCfg @ConfPath("postgres"),
     blockingIOExecutionContext: ExecutionContext @Id("blockingIO"),
     async: Async[F],
     shift: ContextShift[F]
    ): Resource[F, HikariTransactor[F]] = {

     HikariTransactor.newHikariTransactor[F](
       driverClassName = cfg.jdbcDriver
     , url = cfg.url
     , user = cfg.user
     , pass = cfg.password
     , connectEC = blockingIOExecutionContext
     , blocker = Blocker.liftExecutionContext(blockingIOExecutionContext)
     )(async, shift)
  }

  final case class PostgresCfg(
                                jdbcDriver: String
                              , url: String
                              , user: String
                              , password: String
                              , defTimeout: FiniteDuration
                              )

}
