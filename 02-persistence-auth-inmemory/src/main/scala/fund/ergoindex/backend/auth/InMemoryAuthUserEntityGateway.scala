package fund.ergoindex.backend
package auth

import cats.data.EitherT
import cats.effect.IO

import scala.collection.concurrent.TrieMap

object InMemoryAuthUserEntityGateway:
  def make(): AuthUserEntityGateway = new:
    private val db = TrieMap.empty[String, AuthUserEntity]

    override def create(authUser: AuthUserEntity): IO[AuthUserEntity] =
      IO {
        db += (authUser.email -> authUser)
        authUser
      }

    override def get(email: String): EitherT[IO, E, AuthUserEntity] =
      EitherT(IO(db.get(email).toRight(E.UserNotFound)))
