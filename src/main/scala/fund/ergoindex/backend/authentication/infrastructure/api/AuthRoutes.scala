package fund.ergoindex.backend.authentication
package infrastructure.api

import cats.data.EitherT
import cats.effect.IO
import cats.implicits.*

import fund.ergoindex.backend.authentication.domain.api.{
  logInRequestDecoder,
  signUpRequestDecoder,
  LogInRequest,
  SignUpRequest
}
import fund.ergoindex.backend.authentication.domain.models.{AuthToken, AuthUser}
import fund.ergoindex.backend.authentication.domain.ports.{AuthService, AuthUserRepo}

import io.circe.generic.auto.*
import io.circe.syntax.*

import org.http4s.{EntityDecoder, HttpRoutes}
import org.http4s.circe.jsonEncoder
import org.http4s.dsl.io.*

/** Routes for API requests related to authentication.
  */
case class AuthRoutes(authService: AuthService, authUserRepo: AuthUserRepo):

  private def logInRoute = HttpRoutes.of[IO] { case req @ POST -> Root / "authenticate" =>
    val action = for
      logInRequest <- EitherT.right(req.as[LogInRequest])
      authUser     <- authUserRepo.get(logInRequest.email)
      token <- EitherT(
        IO(authService.authenticate(logInRequest.password, authUser))
      )
    yield token

    action.value.flatMap {
      case Right(token) => Ok(token.asJson)
      case Left(errMsg) => BadRequest(errMsg)
    }
  }

  private def signUpRoute = HttpRoutes.of[IO] { case req @ POST -> Root / "signUp" =>
    for
      signUpRequest <- req.as[SignUpRequest]
      passwordHash = signUpRequest.password // TODO: Hash the password
      authUser     = AuthUser(signUpRequest.email, passwordHash)
      _        <- authUserRepo.create(authUser)
      response <- Ok()
    yield response
  }

  def routes = logInRoute <+> signUpRoute
