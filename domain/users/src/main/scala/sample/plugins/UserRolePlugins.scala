package sample.plugins

import izumi.distage.plugins.PluginDef
import sample.modules.UserPersistenceModules.{UserDummyPersistenceBase, UserProductionPersistenceBase}
import sample.modules.UserThirdPartyModules.{UserThirdPartyDummyBase, UserThirdPartyProductionBase}
import sample.modules.{AkkaHttpPluginBase, UserDomainModuleBase}
import zio.IO

class UserPersistenceDummyIOPlugin extends UserDummyPersistenceBase[IO] with PluginDef

class UserPersistenceProductionIOPlugin extends UserProductionPersistenceBase[IO] with PluginDef

class UserThirdPartyDummyIOPlugin extends UserThirdPartyDummyBase[IO] with PluginDef

class UserThirdPartyProductionIOPlugin extends UserThirdPartyProductionBase[IO] with PluginDef

class UserDomainIOPlugin extends UserDomainModuleBase[IO] with PluginDef

class AkkaHttpPlugin extends AkkaHttpPluginBase with PluginDef
