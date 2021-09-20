package fund.ergoindex.backend
package auth

import cats.effect.IO

import scala.collection.concurrent.TrieMap

object InMemoryAuthUserEntityGateway:
  def make(): AuthUserEntityGateway = new:
    private val db = TrieMap.empty[String, AuthUserEntity]

    override def create(authUser: AuthUserEntity): IO[Unit] =
      IO {
        db += (authUser.email -> authUser)
        ()
      }

    override def get(email: String): IO[Option[AuthUserEntity]] =
      IO(db.get(email))
