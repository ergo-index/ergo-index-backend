package fund.ergoindex.backend
package user.infrastructure.api

import cats.effect.IO

import org.http4s.HttpRoutes
import org.http4s.dsl.io.*

/** API routes for user data.
  */
object UserRoutes:
  val routes = HttpRoutes.of[IO] { case req @ GET -> Root / "users" =>
    for response <- Ok()
    yield response
  }
