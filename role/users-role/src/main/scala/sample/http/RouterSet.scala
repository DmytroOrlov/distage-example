package sample.http

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import izumi.functional.bio.BIO._
import izumi.functional.bio.{BIO, BIORunner}
import sample.http.RouterSet.ResponseData
import sample.http.RouterSet.ResponseData._
import sample.http.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

abstract class RouterSet[F[+_, +_]: BIO : BIORunner] {
  def akkaRouter: server.Route


  implicit final class BIOHttpOps[E, Out](bio: F[E, Out]) {

    def asResponse[Err2, Out2](e: E => ResponseData[Err2],
                               out: Out => ResponseData[Out2])
                              (implicit
                               err2dec: Encoder[Err2],
                               out2dec: Encoder[Out2]): StandardRoute = {
      val response = BIORunner[F].unsafeRunSyncAsEither(bio.leftMap(e).map(out)) match {
        case izumi.functional.bio.BIOExit.Success(value) =>
          value.code -> value.data.asJson
        case izumi.functional.bio.BIOExit.Error(exception, _) =>
          exception.code -> exception.data.asJson
        case izumi.functional.bio.BIOExit.Termination(value, _, _) =>
          val resp = internalF(s"Enexpected internal occured. reason: ${value.getMessage}")
          resp.code -> resp.data.asJson
      }
      complete(response)
    }
  }


}

object RouterSet {

  final case class ResponseData[T: Decoder](data: T, code: Int)

  object ResponseData {
    def apply[T: Decoder](f: (Int, T)): ResponseData[T] = new ResponseData(f._2, f._1)

    def success[T: Decoder](data: T) = ResponseData(data, 200)

    case class RestException(message: String)

    def internalF(msg: String) = ResponseData(RestException(msg), 500)

    def notFoundF(msg: String) = RouterSet.ResponseData(RestException(msg), 404)

    def rejectedF(msg: String) = RouterSet.ResponseData(RestException(msg), 403)
  }

}

