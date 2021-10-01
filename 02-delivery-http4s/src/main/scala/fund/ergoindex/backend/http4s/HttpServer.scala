package fund.ergoindex.backend
package http4s

import cats.data.EitherT
import cats.effect.IO

import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder

import java.net.InetSocketAddress

import scala.concurrent.ExecutionContext

trait HttpServer[E]:
  def serve: IO[Unit]

object HttpServer:
  def make[E](
      httpApp: HttpApp[IO],
      executionContext: ExecutionContext,
      host: String,
      port: Int
  ): IO[HttpServer[E]] = IO {
    new:
      override lazy val serve: IO[Unit] =
        BlazeServerBuilder[IO](executionContext)
          .bindSocketAddress(InetSocketAddress(host, port))
          .withHttpApp(httpApp)
          .serve
          .compile
          .drain
  }
