package fund.ergoindex.backend.http4s
package user

import cats.effect.IO

import com.google.firebase.auth.FirebaseToken

import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.dsl.Http4sDsl

object HttpController:
  def make(firebaseAuthMiddleware: FirebaseAuthMiddleware): HttpController =
    new HttpController with Http4sDsl[IO]:
      override lazy val routes: HttpRoutes[IO] = firebaseAuthMiddleware.middleware(userRoutes)

      lazy val userRoutes: AuthedRoutes[FirebaseToken, IO] = AuthedRoutes.of {
        case req @ GET -> Root / "users" as firebaseToken =>
          println(s"reached users route with valid auth firebase token $firebaseToken")
          for response <- Ok()
          yield response
      }
