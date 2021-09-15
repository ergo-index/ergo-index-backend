package fund.ergoindex.backend
package jwt

import cats.effect.{Clock, IO}

enum E:
  case TokenExpired, TokenInvalid

trait JwtController[PrivateKey, PublicKey]:
  def encodeContentIntoJwt(content: String)(using clock: Clock[IO]): IO[String]
  def decodeContentFromJwt(jwt: String): Either[E, String]
