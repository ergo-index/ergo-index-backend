package fund.ergoindex.backend
package auth

import cats.effect.IO

object RedisAuthUserController:
  def make(entityGateway: AuthUserEntityGateway): AuthUserController = new:
    override def create(authUser: AuthUserEntity): IO[Unit] =
      entityGateway.create(authUser)

    override def get(email: String): IO[Option[AuthUserEntity]] = entityGateway.get(email)
