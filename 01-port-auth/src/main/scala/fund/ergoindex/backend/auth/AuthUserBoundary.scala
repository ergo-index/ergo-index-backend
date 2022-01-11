package fund.ergoindex.backend
package auth

import cats.data.EitherT
import cats.effect.IO

trait AuthUserBoundary:
  def create(authUser: AuthUserEntity): IO[Unit]
  def get(email: String): EitherT[IO, E, AuthUserEntity]
  def getIfValidCredentials(email: String, password: String): EitherT[IO, E, AuthUserEntity]

object AuthUserBoundary:
  def make(controller: AuthUserController): AuthUserBoundary = new:
    override def create(authUser: AuthUserEntity): IO[Unit] =
      controller.create(authUser)

    override def get(email: String): EitherT[IO, E, AuthUserEntity] =
      EitherT(controller.get(email).map(_.toRight(E.UserNotFound)))

    override def getIfValidCredentials(
        email: String,
        password: String
    ): EitherT[IO, E, AuthUserEntity] =
      for
        authUser <- get(email)
        validatedAuthUser <-
          if authUser.passwordHash == password then EitherT.rightT[IO, E](authUser)
          else EitherT.leftT[IO, AuthUserEntity](E.InvalidCredentials)
      yield validatedAuthUser
