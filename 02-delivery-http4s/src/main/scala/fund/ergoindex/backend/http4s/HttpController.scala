package fund.ergoindex.backend
package http4s

import cats.effect.IO

import org.http4s.HttpRoutes

trait HttpController:
  def routes: HttpRoutes[IO]
