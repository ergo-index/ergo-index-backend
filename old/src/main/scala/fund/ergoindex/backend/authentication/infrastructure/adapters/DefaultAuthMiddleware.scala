package fund.ergoindex.backend
package authentication.infrastructure.adapters

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global

import fund.ergoindex.backend.authentication.domain.ports.{AuthMiddleware, JwtService}

import org.http4s.{Challenge, HttpRoutes, Request, Status}
import org.http4s.dsl.io.*
import org.http4s.headers.Authorization
import org.http4s.implicits.*

class DefaultAuthMiddleware extends AuthMiddleware:
  def toAuthedRoutes(routes: HttpRoutes[IO], jwtService: JwtService): HttpRoutes[IO] = Kleisli {
    (req: Request[IO]) =>
      routes(req).map {
        case Status.Successful(resp) =>
          val headers = req.headers.get[Authorization]
          val authError =
            Unauthorized(Challenge(scheme = "Bearer", realm = "Please enter a valid API key"))

          headers match
            case Some(authHeader) =>
              val token = authHeader.value.replace("Bearer ", "")
              IO(println("token: " + token)).unsafeRunSync() // TODO: Remove debug
              jwtService.decodeToken(token) match
                case Some(_) => resp
                case None    => authError.unsafeRunSync()
            case None => authError.unsafeRunSync()
        case resp => resp
      }
  }
