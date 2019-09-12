package sample.modules

import izumi.distage.model.definition.ModuleDef
import izumi.distage.model.definition.StandardAxis.Repo
import sample.users.services.UserThirdParty
import sample.users.services.dummy.DummyUserThirdParty
import sample.users.services.production.ProductionUserThirdparty
import distage.TagKK

object UserThirdPartyModules {

  class UserThirdPartyDummyBase[F[+ _, + _]: TagKK] extends ModuleDef {
    tag(Repo.Dummy)

    make[UserThirdParty[F]].from[DummyUserThirdParty[F]]
  }

  class UserThirdPartyProductionBase[F[+ _, + _]: TagKK] extends ModuleDef {
    tag(Repo.Prod)

    make[UserThirdParty[F]].from[ProductionUserThirdparty[F]]
  }
}
