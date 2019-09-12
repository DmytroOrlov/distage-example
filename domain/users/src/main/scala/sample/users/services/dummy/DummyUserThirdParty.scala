package sample.users.services.dummy

import izumi.functional.bio.BIO._
import izumi.functional.bio.BIOError
import sample.Models.CommonFailure
import sample.users.services.UserThirdParty
import sample.users.services.models.UserData

import scala.language.postfixOps

final class DummyUserThirdParty[F[+_, +_]: BIOError] extends UserThirdParty[F] {

  private val idsAllowed : Set[Int] = 1 to 12 toSet

  override def fetchUser(userId: Int): F[CommonFailure, UserData] = {
    F.fromOption(CommonFailure(s"Can't find user by requested id $userId")) {
      idsAllowed.find(_ == userId).map(UserData(_, "firstName", "secondName"))
    }
  }
}
