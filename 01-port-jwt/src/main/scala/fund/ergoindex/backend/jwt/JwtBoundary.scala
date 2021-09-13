package fund.ergoindex.backend
package jwt

import cats.effect.IO

trait JwtBoundary[PrivateKey, PublicKey]:
  def encodeContentIntoJwt(content: String): IO[String]
  def decodeContentFromJwt(jwt: String): IO[Option[String]]

object JwtBoundary:
  def make[PrivateKey, PublicKey](
      controller: JwtController[PrivateKey, PublicKey]
  ): JwtBoundary[PrivateKey, PublicKey] = new:
    override def encodeContentIntoJwt(content: String): IO[String] =
      controller.encodeContentIntoJwt(content)
    override def decodeContentFromJwt(jwt: String): IO[Option[String]] =
      controller.decodeContentFromJwt(jwt)
