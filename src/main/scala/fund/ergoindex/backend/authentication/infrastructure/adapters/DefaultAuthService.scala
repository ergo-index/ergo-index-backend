package fund.ergoindex.backend
package authentication.infrastructure.adapters

import fund.ergoindex.backend.authentication.domain.api.LogInRequest
import fund.ergoindex.backend.authentication.domain.models.{AuthToken, AuthUser}
import fund.ergoindex.backend.authentication.domain.ports.{AuthService, JwtService}

class DefaultAuthService(jwtService: JwtService) extends AuthService:
  // TODO: Verify against a Redis database provided by JwtService
  def authenticate(password: String, authUser: AuthUser): Either[String, String] =
    if authUser.passwordHash == password then
      Right(jwtService.encodeToken(AuthToken(authUser.email)))
    else Left("Password hash does not match")
