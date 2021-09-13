package fund.ergoindex.backend
package auth

import cats.data.EitherT
import cats.effect.IO

trait AuthUserBoundary:
  def create(authUser: AuthUserEntity): IO[AuthUserEntity]
  def get(email: String): EitherT[IO, E, AuthUserEntity]
  def getIfValidCredentials(email: String, password: String): EitherT[IO, E, AuthUserEntity]

object AuthUserBoundary:
  def make(controller: AuthUserController): AuthUserBoundary = new:
    override def create(authUser: AuthUserEntity): IO[AuthUserEntity] =
      controller.create(authUser)

    override def get(email: String): EitherT[IO, E, AuthUserEntity] = controller.get(email)

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
