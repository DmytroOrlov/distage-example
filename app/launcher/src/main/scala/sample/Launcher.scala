package sample

import izumi.distage.roles.RoleAppMain
import zio.IO

object Launcher extends RoleAppMain.Default[IO[Throwable, ?]](DistageApp) {
  override def main(args: Array[String]): Unit = {
    val targs = Array(":users", "-c", "./app/launcher/src/main/resources/application.conf")
    super.main(targs ++ args)
  }
}
