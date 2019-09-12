package sample.users.services.production

import doobie.ConnectionIO
import doobie.implicits._
import izumi.functional.bio.BIO._
import izumi.functional.bio.BIOBifunctor
import sample.Models.CommonFailure
import sample.users.services.models.{Email, User, UserData}
import sample.users.services.production.PostgresUserPersistence.queries
import sample.users.services.{UserPersistence, models}

final class PostgresUserPersistence[F[+_, +_]: BIOBifunctor]
(
  pgConnection: PostgresConnector[F]
) extends UserPersistence[F] {

  override def upsert(user: models.User): F[CommonFailure, Unit] = {
    pgConnection.query("upserting-new-user")(queries.upsertUser(user))
      .leftMap(ec => CommonFailure(s"Error while register into DB ${ec.msg}"))
      .void
  }

  override def remove(userId: Email): F[CommonFailure, Unit] = {
    pgConnection.query("deleting-user")(queries.deleteuser(userId))
      .leftMap(ec => CommonFailure(s"error while removing from DB. Reason: ${ec.msg}"))
      .void
  }

  override def get(userId: Email): F[CommonFailure, models.User] = {
    pgConnection.query("retrieve")(queries.fetchById(userId))
      .leftMap(ec => CommonFailure(s"error while getting from DB user. reason: ${ec.msg}"))
  }
}

object PostgresUserPersistence {
  object queries {
    def upsertUser(user: User): ConnectionIO[Int] = {
      sql"""
           |insert into public.distage_sample (email_id, id, first_name, second_name) values (${user.email}, ${user.data.id}, ${user.data.firstName}, ${user.data.secondName})
           | on conflict (email_id) do update
           | set id = excluded.id, first_name = excluded.first_name, second_name = excluded.second_name;
       """.stripMargin.update.run
    }

    def fetchById(userId: Email): ConnectionIO[User] = {
      sql"""select id, first_name, second_name from public.distage_sample where email_id = $userId;"""
        .stripMargin
        .query[UserData]
        .map(User(userId, _))
        .unique
    }

    def cleanupTable(): ConnectionIO[Int] = {
      sql"truncate table public.distage_sample;".update.run
    }


    def deleteuser(email: Email): ConnectionIO[Int] = {
      sql"delete from public.distage_sample where email_id = $email;".update.run
    }

  }
}
