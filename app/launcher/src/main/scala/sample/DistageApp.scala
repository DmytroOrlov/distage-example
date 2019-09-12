package sample

import izumi.distage.model.definition.StandardAxis.Repo
import izumi.distage.model.definition.{Axis, AxisBase}
import izumi.distage.plugins.load.PluginLoader.PluginConfig
import izumi.distage.roles.model.meta.LibraryReference
import izumi.distage.roles.{BootstrapConfig, RoleAppLauncher}
import zio.IO
import zio.interop.catz._

class DistageApp extends RoleAppLauncher.LauncherF[IO[Throwable, ?]] {
  override val bootstrapConfig = BootstrapConfig(
    PluginConfig(
      debug = false
    , packagesEnabled = Seq("sample.plugins")
    , packagesDisabled = Seq.empty
    )
  )

  override protected def referenceLibraryInfo: Seq[LibraryReference] = {
    Seq(
      LibraryReference("izumi-workshop-01-master", classOf[DistageApp])
    )
  }

  override protected def defaultActivations: Map[AxisBase, Axis.AxisValue] =
    Map(Repo -> Repo.Prod)
}

object DistageApp extends DistageApp
