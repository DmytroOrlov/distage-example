package sample.users.services

import sample.Models.CommonFailure
import sample.users.services.models.UserData

trait UserThirdParty[F[+_, +_]] {
  def fetchUser(userId: Int) : F[CommonFailure, UserData]
}
