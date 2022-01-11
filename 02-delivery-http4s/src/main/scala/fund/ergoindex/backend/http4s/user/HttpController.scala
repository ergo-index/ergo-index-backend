package fund.ergoindex.backend.http4s
package user

import cats.effect.IO

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object HttpController:
  def make(authMiddleware: AuthMiddleware): HttpController =
    new HttpController with Http4sDsl[IO]:
      override lazy val routes: HttpRoutes[IO] = authMiddleware.toAuthedRoutes(userRoute)

      lazy val userRoute = HttpRoutes.of[IO] { case req @ GET -> Root / "users" =>
        for response <- Ok()
        yield response
      }
