package fund.ergoindex.backend
package http4s

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global // TODO: Remove after debug messages are gone

import fund.ergoindex.backend.jwt.{E => JwtE, JwtBoundary}

import org.http4s.{Challenge, HttpRoutes, Request, Status}
import org.http4s.dsl.io.*
import org.http4s.headers.Authorization
import org.http4s.implicits.*

trait AuthMiddleware:
  def toAuthedRoutes(routes: HttpRoutes[IO]): HttpRoutes[IO]

object AuthMiddleware:
  def make[PrivateKey, PublicKey](
      jwtBoundary: JwtBoundary[PrivateKey, PublicKey]
  ): AuthMiddleware = new:
    override def toAuthedRoutes(routes: HttpRoutes[IO]): HttpRoutes[IO] = Kleisli {
      (req: Request[IO]) =>
        routes(req).map {
          case Status.Successful(resp) =>
            val headers = req.headers.get[Authorization]
            val authError =
              Unauthorized(Challenge(scheme = "Bearer", realm = "Please enter a valid API key"))

            headers match
              case Some(authHeader) =>
                val jwt = authHeader.value.replace("Bearer ", "")
                jwtBoundary.decodeContentFromJwt(jwt) match
                  case Right(content) =>
                    println("decoded JWT content: " + content) // TODO: Remove debug
                    resp
                  case Left(err: JwtE) =>
                    println("err: " + err) // TODO: Remove debug
                    resp
              case None => authError.unsafeRunSync() // TODO: Remove unsafe
          case resp => resp
        }
    }
