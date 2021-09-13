package fund.ergoindex.backend
package auth

import cats.data.EitherT
import cats.effect.IO

trait AuthUserController:
  def create(authUser: AuthUserEntity): IO[AuthUserEntity]
  def get(email: String): EitherT[IO, E, AuthUserEntity]