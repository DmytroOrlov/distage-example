import izumi.sbt.deps.IzumiDeps
import izumi.sbt.deps.IzumiDeps.{R, V}
import sbt.Keys.libraryDependencies

version in ThisBuild := "1.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.13.0"
organization in ThisBuild := "io.7mind"

enablePlugins(IzumiGitEnvironmentPlugin)

val GlobalSettings = new DefaultGlobalSettingsGroup {
  override val settings: Seq[sbt.Setting[_]] = Seq(
    crossScalaVersions := Seq(
      V.scala_213,
    ),
    addCompilerPlugin(R.kind_projector),

    libraryDependencies ++= Seq(
      Izumi.R.distage_plugins,
      IzumiDeps.T.scalatest,
      Izumi.R.fundamentals_bio,
      Izumi.R.logstage_di,
      Izumi.R.distage_testkit,
    ) ++ IzumiDeps.R.cats_all,
  )
}

// exclude assembly for libraries
lazy val WithoutBadPlugins = new SettingsGroup {
  override val disabledPlugins: Set[sbt.AutoPlugin] = Set(AssemblyPlugin)
}

lazy val DomainSettings = new SettingsGroup {
  override def settings: Seq[sbt.Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      Izumi.R.distage_roles
    )
  )
}

lazy val AppSettings = new SettingsGroup {
  override val plugins: Set[sbt.Plugins] = Set(AssemblyPlugin)
  override val settings: Seq[sbt.Setting[_]] = Seq(
    libraryDependencies ++= Seq(Izumi.R.distage_roles),
  )
}

lazy val RoleSettings = new SettingsGroup {
  override val settings: Seq[sbt.Setting[_]] = Seq(
    libraryDependencies ++= Seq(Izumi.R.distage_roles_api, Izumi.R.distage_config),
  )
}

val SbtSettings = new SettingsGroup {
  override val settings: Seq[sbt.Setting[_]] = Seq(
    Seq(
      target ~= { t => t.toPath.resolve("primary").toFile },
      crossScalaVersions := Seq(V.scala_212),
      libraryDependencies ++= Seq(
        "org.scala-sbt" % "sbt" % sbtVersion.value
      ),
      sbtPlugin := true,
    )
  ).flatten
}

lazy val inRoot = In(".").settings(WithoutBadPlugins)

lazy val inDomain = In("domain").settings(GlobalSettings, WithoutBadPlugins, DomainSettings)

lazy val inLib = In("lib").settings(GlobalSettings, WithoutBadPlugins)

lazy val inRoles = In("role").settings(GlobalSettings, RoleSettings, WithoutBadPlugins)

lazy val inApp = In("app").settings(GlobalSettings, AppSettings)

lazy val inSbt = In("sbt").settings(SbtSettings, WithoutBadPlugins)

lazy val common = inLib.as.module
  .settings(
    libraryDependencies ++= Seq(
      IzumiDeps.R.zio_interop,
      IzumiDeps.R.zio_core,
    )
  )

lazy val users = inDomain.as.module
  .depends(common)
  .settings(
    libraryDependencies ++= Seq(
      "org.tpolecat"      %% "doobie-core"       % "0.8.0-RC1",
      "org.tpolecat"      %% "doobie-hikari"     % "0.8.0-RC1",
      "org.tpolecat"      %% "doobie-postgres"   % "0.8.0-RC1",
      "org.tpolecat"      %% "doobie-scalatest"  % "0.8.0-RC1" % Test,
      "com.typesafe.akka" %% "akka-actor"        % "2.5.25",
      "com.typesafe.akka" %% "akka-stream"       % "2.5.25",
      "com.typesafe.akka" %% "akka-http"         % "10.1.9",
      "com.typesafe.akka" %% "akka-http-testkit" % "10.1.9"    % Test,
    )
  )

lazy val usersRole = inRoles.as.module
  .depends(users)

lazy val launcher = inApp.as.module
  .depends(usersRole)

lazy val sbtBomDistageSample = inSbt.as
  .module
  .settings(withBuildInfo("sample", "DistageSample"))

lazy val workshop = inRoot.as.root
  .settings(name := "distage-sample")
  .transitiveAggregate(launcher, sbtBomDistageSample)

/*
At this point use thse commands to setup project layout from sbt shell:

newModule role/accounts-role
newModule role/users-role
newModule app/launcher
*/
