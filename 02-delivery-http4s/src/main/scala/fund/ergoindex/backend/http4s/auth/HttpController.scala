package fund.ergoindex.backend.http4s
package auth

import cats.data.EitherT
import cats.effect.IO
import cats.implicits.*

import fund.ergoindex.backend.auth.{AuthUserBoundary, AuthUserEntity, E => AuthE}
import fund.ergoindex.backend.jwt.{E => JwtE, JwtBoundary}

import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*

import org.http4s.{EntityDecoder, HttpRoutes}
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.*
import org.http4s.implicits.*

object HttpController:
  def make[PrivateKey, PublicKey](
      jwtBoundary: JwtBoundary[PrivateKey, PublicKey],
      authUserBoundary: AuthUserBoundary
  ): HttpController =
    new HttpController with Http4sDsl[IO]:
      override lazy val routes: HttpRoutes[IO] = logInRoute <+> signUpRoute

      object request:
        final case class LogIn(email: String, password: String)
        final case class SignUp(email: String, password: String)

        given Decoder[LogIn]            = deriveDecoder
        given Decoder[SignUp]           = deriveDecoder
        given EntityDecoder[IO, LogIn]  = jsonOf[IO, LogIn]
        given EntityDecoder[IO, SignUp] = jsonOf[IO, SignUp]

      lazy val logInRoute = HttpRoutes.of[IO] { case req @ POST -> Root / "authenticate" =>
        val action =
          for
            logInRequest <- EitherT.right(req.as[request.LogIn])
            authUser <- authUserBoundary.getIfValidCredentials(
              logInRequest.email,
              logInRequest.password
            )
            jwt <- EitherT.right(
              jwtBoundary.encodeContentIntoJwt("{\"email\":\"" + authUser.email + "\"}")
            )
          yield jwt

        action.value.flatMap {
          case Right(jwt) => Ok(jwt.asJson)
          case Left(e) =>
            BadRequest(e match
              case AuthE.UserNotFound       => "No user with that email address could be found"
              case AuthE.InvalidCredentials => "Invalid credentials"
            )
        }
      }

      lazy val signUpRoute = HttpRoutes.of[IO] { case req @ POST -> Root / "signUp" =>
        for
          signUpRequest <- req.as[request.SignUp]
          passwordHash = signUpRequest.password // TODO: Hash the password
          authUser     = AuthUserEntity(signUpRequest.email, passwordHash)
          _        <- authUserBoundary.create(authUser)
          response <- Ok()
        yield response
      }
