package sample

import izumi.distage.model.definition.StandardAxis.Repo
import izumi.distage.model.definition.{Axis, AxisBase}
import izumi.distage.testkit.services.st.adapter.DISyntaxBIO
import izumi.distage.testkit.st.adapter.specs.DistagePluginBioSpec
import izumi.fundamentals.platform.build.ExposedTestScope
import org.scalatest.Assertion
import zio.IO

import scala.util.Random

@ExposedTestScope
abstract class TestBIO
  extends DistagePluginBioSpec[IO]
    with DISyntaxBIO[IO] {

  def dummy: Boolean

  override protected def pluginPackages: Seq[String] = {
    Seq("sample.plugins")
  }

  override protected def memoizePlugins: Boolean = false

  override protected def activation: Map[AxisBase, Axis.AxisValue] = {
    if (!dummy) Map(Repo -> Repo.Prod)
    else        Map(Repo -> Repo.Dummy)
  }
}

@ExposedTestScope
trait RandomSpec {
  this: Assertion =>

  trait Random[T] {
    def perform(): T
  }

  implicit def randomEmail: Random[Email] = {
    () => {
      Email(s"${Random.nextString(5)}@${Random.nextString(3)}.com")
    }
  }

  implicit def randomString: Random[String] = {
    () => {
      s"${Random.nextString(5)}"
    }
  }

  implicit def randomInt: Random[Int] = {
    () => {
      Random.nextInt()
    }
  }

  def random[T: Random]: T = implicitly[Random[T]].perform()

  type Email = RandomSpec.Email
  val Email = RandomSpec.Email
}

object RandomSpec {
  final case class Email(get: String) extends AnyVal
}
