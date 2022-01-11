package fund.ergoindex.backend
package jwt

import cats.effect.{Clock, IO}

trait JwtBoundary[PrivateKey, PublicKey]:
  def encodeContentIntoJwt(content: String)(using clock: Clock[IO]): IO[String]
  def decodeContentFromJwt(jwt: String): Either[E, String]

object JwtBoundary:
  def make[PrivateKey, PublicKey](
      controller: JwtController[PrivateKey, PublicKey]
  ): JwtBoundary[PrivateKey, PublicKey] = new:
    override def encodeContentIntoJwt(content: String)(using clock: Clock[IO]): IO[String] =
      controller.encodeContentIntoJwt(content)
    override def decodeContentFromJwt(jwt: String): Either[E, String] =
      controller.decodeContentFromJwt(jwt)
