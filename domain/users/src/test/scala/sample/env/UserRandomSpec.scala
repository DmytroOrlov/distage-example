package sample.env

import org.scalatest.Assertion
import sample.RandomSpec
import sample.users.services.models
import sample.users.services.models.{User, UserData}

trait UserRandomSpec extends RandomSpec {
  this: Assertion =>
  implicit def randoUserData: Random[UserData] = {
    () => {
      UserData(implicitly[Random[Int]].perform(), implicitly[Random[String]].perform(), implicitly[Random[String]].perform())
    }
  }

  implicit def randomUserUser: Random[User] = {
    () => {
      models.User(implicitly[Random[Email]].perform().get, implicitly[Random[UserData]].perform())
    }
  }
}
