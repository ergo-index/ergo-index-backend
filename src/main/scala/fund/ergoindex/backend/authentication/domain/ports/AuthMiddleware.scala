package fund.ergoindex.backend
package authentication.domain.ports

import cats.effect.IO

import fund.ergoindex.backend.authentication.domain.ports.JwtService

import org.http4s.HttpRoutes

/** Middleware to protect endpoints with authentication.
  */
trait AuthMiddleware:
  /** Requires API users to provide authentication in order to use the given routes.
    *
    * @param routes the routes that are initially not protected
    * @param jwtService the implementation of JwtService being used
    * @return the given routes, with logic to check for authentication
    */
  def toAuthedRoutes(routes: HttpRoutes[IO], jwtService: JwtService): HttpRoutes[IO]
