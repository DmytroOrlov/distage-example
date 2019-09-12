package sample.http.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}
import io.circe.generic.auto._
import izumi.functional.bio.{BIO, BIORunner}
import sample.http.RouterSet
import sample.http.RouterSet.ResponseData._
import sample.http.akkahttpcirce.FailFastCirceSupport._
import sample.http.routes.UserServiceRest.ExternalUserRequest
import sample.users.services.UserService

class UserServiceRest[F[+_, +_]: BIO: BIORunner]
(
  userService: UserService[F]
) extends RouterSet[F] {
  override def akkaRouter: Route = {
    pathPrefix("users") {
      (post & pathEnd & entity(as[ExternalUserRequest])) {
        req =>
          userService.upsert(req.id, req.email)
            .asResponse(
              f => internalF(f.reason),
              _ => success(s"Successfully overwritten user with email=${req.email}")
            )
      } ~ path(UserServiceRest.EmailMatcher) {
        email => {
          get {
            userService.retrieve(email)
              .asResponse(f => notFoundF(s"error ${f.reason}"), success)
          } ~
            delete {
              userService.delete(email)
                .asResponse(
                  f => internalF(f.reason),
                  _ => success("Successfully cleaned up")
                )
            }
        }
      }
    }
  }
}

object UserServiceRest {

  val EmailMatcher: PathMatcher1[String] =
    PathMatcher("""\w+@[\w\.]+""".r) flatMap {
      string =>
        try Some(string)
        catch {
          case _: IllegalArgumentException => None
        }
    }

  type Email = String

  case class ExternalUserRequest(email: String, id: Int)

}
