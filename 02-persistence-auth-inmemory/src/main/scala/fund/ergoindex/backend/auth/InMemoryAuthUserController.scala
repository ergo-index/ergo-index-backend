package fund.ergoindex.backend
package auth

import cats.data.EitherT
import cats.effect.IO

object InMemoryAuthUserController:
  def make(entityGateway: AuthUserEntityGateway): AuthUserController = new:
    override def create(authUser: AuthUserEntity): IO[AuthUserEntity] =
      entityGateway.create(authUser)

    override def get(email: String): EitherT[IO, E, AuthUserEntity] = entityGateway.get(email)
