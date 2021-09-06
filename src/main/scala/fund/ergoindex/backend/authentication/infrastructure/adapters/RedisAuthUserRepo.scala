package fund.ergoindex.backend
package authentication.infrastructure.adapters

import cats.data.EitherT
import cats.effect.IO

import fund.ergoindex.backend.authentication.domain.models.AuthUser
import fund.ergoindex.backend.authentication.domain.ports.AuthUserRepo

import scala.collection.concurrent.TrieMap
import scala.util.{Success, Try}

// TODO: Use Redis instead of in-memory db
class RedisAuthUserRepo extends AuthUserRepo:
  private val db = TrieMap.empty[String, AuthUser]

  def create(authUser: AuthUser): IO[AuthUser] =
    IO {
      db += (authUser.email -> authUser)
      authUser
    }

  def get(email: String): EitherT[IO, String, AuthUser] =
    val getIo = IO(db.get(email).toRight("Email `" + email + "' not found"))
    EitherT(getIo)
