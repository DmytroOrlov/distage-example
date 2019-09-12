package sample.users.services.production

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, _}
import akka.stream.Materializer
import akka.util.ByteString
import io.circe.DecodingFailure
import io.circe.parser.parse
import izumi.distage.model.definition.Id
import izumi.functional.bio.BIO
import izumi.functional.bio.BIO._
import sample.Models
import sample.Models.CommonFailure
import sample.plugins.BIOFromFuture
import sample.users.services.models.UserData
import sample.users.services.{UserThirdParty, models}

import scala.concurrent.ExecutionContext

final class ProductionUserThirdparty[F[+ _, + _]: BIO: BIOFromFuture]
(implicit
  as: ActorSystem,
  mat: Materializer,
  ec: ExecutionContext@Id("akka-ec")
) extends UserThirdParty[F] {
  override def fetchUser(userId: Int): F[Models.CommonFailure, models.UserData] = {
    val request = HttpRequest(HttpMethods.GET, Uri(s"https://reqres.in/api/users/$userId"))

    val put = F.syncThrowable {
      Http().singleRequest(request).flatMap {
        response => {
          response.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
            .map(_.utf8String)
            .map(str => {
              F.fromEither(parse(str))
            })
        }
      }
    }

    BIOFromFuture[F]
      .fromFuture(put)
      .flatten
      .leftMap(thr => CommonFailure(s"error while performing http request. ${thr.getMessage}"))
      .flatMap {
        json =>
          val parsed = json.hcursor.downField("data").focus.map(_.as[UserData])
            .getOrElse(Left(DecodingFailure.apply("Error while parsing", Nil)))

          F.fromEither(parsed)
            .leftMap(f => CommonFailure(s"Error while fetching user from REST API. ${f.getMessage}"))
      }
  }
}
