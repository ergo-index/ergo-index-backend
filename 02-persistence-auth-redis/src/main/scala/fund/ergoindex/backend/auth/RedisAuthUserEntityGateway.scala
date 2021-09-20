package fund.ergoindex.backend
package auth

import cats.effect.IO

import scala.collection.concurrent.TrieMap

import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.effect.Log.Stdout.*

object RedisAuthUserEntityGateway:
  def make(redis: RedisCommands[IO, String, String]): AuthUserEntityGateway = new:
    override def create(authUser: AuthUserEntity): IO[Unit] =
      redis.set(authUser.email, authUser.passwordHash)

    override def get(email: String): IO[Option[AuthUserEntity]] =
      redis
        .get(email)
        .map(_.map(passwordHash => AuthUserEntity(email, passwordHash)))
