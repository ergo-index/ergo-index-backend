package fund.ergoindex.backend
package jwt

import cats.effect.IO

trait JwtController[PrivateKey, PublicKey]:
  def encodeContentIntoJwt(content: String): IO[String]
  def decodeContentFromJwt(jwt: String): IO[Option[String]]
