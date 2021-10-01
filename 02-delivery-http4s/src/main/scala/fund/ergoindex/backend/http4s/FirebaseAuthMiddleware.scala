package fund.ergoindex.backend
package http4s

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import cats.effect.unsafe.implicits.global // TODO: Remove after debug messages are gone

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.{FirebaseAuthException, FirebaseToken}

import fund.ergoindex.backend.firebase.FirebaseBoundary

import org.http4s.{AuthedRequest, AuthedRoutes, Challenge, HttpRoutes, Request, Response, Status}
import org.http4s.dsl.io.*
import org.http4s.headers.Authorization
import org.http4s.implicits.*
import org.http4s.server.AuthMiddleware

import scala.util.{Failure, Success}

sealed trait Error
case object AnyError extends Error

trait FirebaseAuthMiddleware:
  def middleware: AuthMiddleware[IO, FirebaseToken]

object FirebaseAuthMiddleware:
  def make(
      firebaseApp: FirebaseApp,
      firebaseBoundary: FirebaseBoundary
  ): FirebaseAuthMiddleware = new:
    def onAuthFailure: AuthedRoutes[Error, IO] = Kleisli { (req: AuthedRequest[IO, Error]) =>
      req.req match
        case _ =>
          OptionT.pure[IO](Response[IO](status = Status.Unauthorized))
    }

    def getFirebaseTokenFromHeader(authHeader: String): IO[Option[FirebaseToken]] =
      val bearerToken = authHeader.replace("Bearer ", "")
      println(s"Getting firebase token from bearer token $bearerToken")
      firebaseBoundary
        .verifyIdToken(bearerToken, firebaseApp)
        .flatMap(idTokenTry =>
          IO {
            idTokenTry match
              case Success(token: FirebaseToken)       => Some(token)
              case Failure(err: FirebaseAuthException) => None
              case Failure(err: Throwable) =>
                println("SEVERE: Unexpected error thrown while authenticating")
                err.printStackTrace
                None
          }
        )

    def authUser: Kleisli[IO, Request[IO], Either[Error, FirebaseToken]] = Kleisli {
      (request: Request[IO]) =>
        request.headers.get[Authorization] match
          case Some(authHeader) =>
            getFirebaseTokenFromHeader(authHeader.value).map(_.toRight(AnyError))
          case None => IO.pure(Left(AnyError))
    }

    override val middleware: AuthMiddleware[IO, FirebaseToken] =
      AuthMiddleware(authUser, onAuthFailure)
