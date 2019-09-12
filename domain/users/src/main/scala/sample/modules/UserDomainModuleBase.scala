package sample.modules

import sample.users.services.UserService
import distage.{ModuleDef, TagKK}

class UserDomainModuleBase[F[+_, +_]: TagKK] extends ModuleDef {
  make[UserService[F]]
}
