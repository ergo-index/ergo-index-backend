package fund.ergoindex.backend.http4s
package auth

import cats.effect.IO

import org.http4s.HttpRoutes

object HttpBoundary:
  def make(controller: HttpController): HttpBoundary = new:
    override lazy val routes: HttpRoutes[IO] = controller.routes
