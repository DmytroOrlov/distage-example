package sample.plugins

import izumi.distage.plugins.PluginDef
import sample.UsersRole
import sample.http.routes.UserServiceRest
import sample.http.{HttpServerLauncher, RouterSet}
import sample.UsersRole
import sample.http.RouterSet
import zio.IO

class UserRolePlugin extends PluginDef {
  make[UsersRole[IO]]
}

class HttpIOPlugin extends PluginDef {
  many[RouterSet[IO]]
      .add[UserServiceRest[IO]]
  make[HttpServerLauncher.StartedServer].fromResource[HttpServerLauncher[IO]]
}
