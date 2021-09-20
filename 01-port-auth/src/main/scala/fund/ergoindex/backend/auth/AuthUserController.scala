package fund.ergoindex.backend
package auth

import cats.effect.IO

trait AuthUserController:
  def create(authUser: AuthUserEntity): IO[Unit]
  def get(email: String): IO[Option[AuthUserEntity]]
