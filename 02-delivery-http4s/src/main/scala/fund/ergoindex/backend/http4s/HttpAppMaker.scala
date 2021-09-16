package fund.ergoindex.backend
package http4s

import cats.effect.IO
import cats.implicits.*

import org.http4s.HttpApp
import org.http4s.implicits.*
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

import scala.util.chaining.scalaUtilChainingOps

object HttpAppMaker:
  def make(first: HttpBoundary, remaining: HttpBoundary*): HttpApp[IO] =
    (first +: remaining)
      .map(_.routes)
      .reduceLeft(_ <+> _)
      .pipe(routes => Router("api" -> routes))
      .orNotFound
      .pipe(Logger.httpApp(logHeaders = true, logBody = true)) // TODO: Remove this in production
