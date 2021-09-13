package fund.ergoindex.backend
package authentication.domain.ports

import cats.effect.IO

import fund.ergoindex.backend.authentication.domain.models.AuthUser

/** Repository for [[fund.ergoindex.backend.authentication.domain.models.AuthUser]]s.
  */
trait AuthUserRepo:
  def create(authUser: AuthUser): IO[AuthUser]
  def get(email: String): IO[Either[String, AuthUser]]
