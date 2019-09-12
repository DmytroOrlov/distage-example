package sample.users.services.dummy

import izumi.functional.bio.BIO
import izumi.functional.bio.BIO._
import sample.Models.CommonFailure
import sample.users.services.UserPersistence
import sample.users.services.models.{Email, User}

import scala.collection.mutable

class DummyUserPersistence[F[+_, +_]: BIO] extends UserPersistence[F] {

  private val storage = mutable.HashMap.empty[Email, User]

  override def upsert(user: User): F[CommonFailure, Unit] = {
    syncBIO(storage.update(user.email, user))
  }

  override def remove(userId: Email): F[CommonFailure, Unit] = {
    syncBIO(storage.remove(userId))
      .void
  }

  override def get(userId: Email): F[CommonFailure, User] = {
    syncBIO(storage(userId))
  }

  private[this] def syncBIO[T](f: => T): F[CommonFailure, T] = {
    F.syncThrowable(synchronized(f)).leftMap(CommonFailure(_))
  }
}
