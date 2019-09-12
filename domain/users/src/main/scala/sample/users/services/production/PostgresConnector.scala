package sample.users.services.production

import doobie.free.connection.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.syntax.connectionio._
import izumi.functional.bio.BIO._
import izumi.functional.bio.BIO.catz._
import izumi.functional.bio.BIOPanic
import sample.users.services.production.PostgresException.QueryException

trait PostgresConnector[F[+_, +_]] {
  def query[T](metaName: String)(query: ConnectionIO[T]): F[PostgresException, T]
}

object PostgresConnector {

  final class Impl[F[+_, +_]: BIOPanic]
  (
    transactor: HikariTransactor[F[Throwable, ?]]
  ) extends PostgresConnector[F]  {

    override def query[T](metaName: String)(query: ConnectionIO[T]): F[PostgresException, T] = {
      query.transact(transactor)
        .sandbox
        .leftMap(_.toThrowable)
        .leftMap(QueryException(_))
    }
  }

}

sealed trait PostgresException {
  def msg: String
}

object PostgresException {

  final case class QueryException(msg: String) extends PostgresException
  object QueryException {
    def apply(err: Throwable): QueryException = new QueryException(err.getMessage)
  }

}
