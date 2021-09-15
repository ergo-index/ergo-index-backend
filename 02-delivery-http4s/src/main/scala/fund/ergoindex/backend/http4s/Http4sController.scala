package fund.ergoindex.backend
package http4s

import cats.data.{EitherT, Kleisli}
import cats.effect.IO
import cats.effect.unsafe.implicits.global // TODO: Remove after debug messages are gone
import cats.implicits.*

import fund.ergoindex.backend.auth.{AuthUserBoundary, AuthUserEntity, E => AuthE}
import fund.ergoindex.backend.jwt.{E => JwtE, JwtBoundary}

import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*

import org.http4s.{Challenge, EntityDecoder, HttpRoutes, Request, Status}
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.*
import org.http4s.headers.Authorization
import org.http4s.implicits.*
import org.http4s.server.Router

trait Http4sController:
  def routes: HttpRoutes[IO]

object Http4sController:
  def make[PrivateKey, PublicKey](
      jwtBoundary: JwtBoundary[PrivateKey, PublicKey],
      authUserBoundary: AuthUserBoundary
  ): Http4sController =
    new Http4sController with Http4sDsl[IO]:
      override lazy val routes: HttpRoutes[IO] =
        logInRoute <+> signUpRoute <+> toAuthedRoutes(userRoute)

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

      lazy val userRoute = HttpRoutes.of[IO] { case req @ GET -> Root / "users" =>
        for response <- Ok()
        yield response
      }

      def toAuthedRoutes(routes: HttpRoutes[IO]): HttpRoutes[IO] = Kleisli { (req: Request[IO]) =>
        routes(req).map {
          case Status.Successful(resp) =>
            val headers = req.headers.get[Authorization]
            val authError =
              Unauthorized(Challenge(scheme = "Bearer", realm = "Please enter a valid API key"))

            headers match
              case Some(authHeader) =>
                val jwt = authHeader.value.replace("Bearer ", "")
                jwtBoundary.decodeContentFromJwt(jwt) match
                  case Right(content) =>
                    println("decoded JWT content: " + content) // TODO: Remove debug
                    resp
                  case Left(err: JwtE) =>
                    println("err: " + err) // TODO: Remove debug
                    resp
              case None => authError.unsafeRunSync() // TODO: Remove unsafe
          case resp =>
            resp
        }
      }

      object request:
        final case class LogIn(email: String, password: String)
        final case class SignUp(email: String, password: String)

        given Decoder[LogIn]            = deriveDecoder
        given Decoder[SignUp]           = deriveDecoder
        given EntityDecoder[IO, LogIn]  = jsonOf[IO, LogIn]
        given EntityDecoder[IO, SignUp] = jsonOf[IO, SignUp]
