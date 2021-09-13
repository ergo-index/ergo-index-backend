package fund.ergoindex.backend
package http4s

import cats.effect.IO

import org.http4s.HttpRoutes

trait Http4sBoundary:
  def routes: HttpRoutes[IO]

object Http4sBoundary:
  def make(controller: Http4sController): Http4sBoundary = new:
    override def routes: HttpRoutes[IO] = controller.routes
