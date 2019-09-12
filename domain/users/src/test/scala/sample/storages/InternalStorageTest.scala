package sample.storages

import izumi.distage.plugins.PluginDef
import izumi.functional.bio.BIO._
import org.scalatest.Assertion
import sample.env.UserRandomSpec
import sample.storages.InternalStorageTest.Ctx
import sample.users.services.UserPersistence
import sample.users.services.models.User
import sample.users.services.production.PostgresDataSource.PostgresCfg
import sample.{RandomSpec, TestBIO}
import zio.IO

import scala.concurrent.duration._

class PGPlugin extends PluginDef {
  make[PostgresCfg].from {
    PostgresCfg(
      jdbcDriver = "org.postgresql.Driver"
    , url = "jdbc:postgresql://localhost/distage"
    , user = "distage"
    , password = "distage"
    , defTimeout = 20.seconds
    )
  }
}
abstract class InternalStorageTest extends TestBIO
  with Assertion
  with RandomSpec with UserRandomSpec {

  "internal storage" must {

    "upsert correctly" in dio {
      ctx: Ctx =>
        import ctx.storage

        val testEmail = random[Email].get
        val userData1 = random[User].copy(email = testEmail)

        for {
          _ <- storage.upsert(userData1)
          read1 <- storage.get(testEmail)
          _ = assert(read1 == userData1)
          userData2 = random[User].copy(email = testEmail)
          _ <- storage.upsert(userData2)
          read2 <- storage.get(testEmail)
          _ = assert(read2 == userData2)
        } yield {
        }

    }

    "delete correctly" in dio {
      ctx: Ctx =>
        import ctx.storage

        val testEmail = random[Email].get
        val userData = random[User].copy(email = testEmail)
        for {
          _ <- storage.upsert(userData)
          read1 <- storage.get(testEmail).redeemPure(_ => None, Some(_))
          _ = assert(read1.contains(userData))
          _ <- storage.remove(testEmail)
          read2 <- storage.get(testEmail).redeemPure(_ => None, Some(_))
          _ = assert(read2.isEmpty)
        } yield ()

    }
  }
}

object InternalStorageTest {

  case class Ctx(storage: UserPersistence[IO])

}


final class DummyInternalStorage extends InternalStorageTest {
  override val dummy: Boolean = true
}

final class ProductionInternalStorage extends InternalStorageTest {
  override val dummy: Boolean = false
}
