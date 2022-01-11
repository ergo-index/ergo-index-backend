package fund.ergoindex.backend
package jwt

import cats.effect.{Clock, IO}

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.*

import java.security.{PrivateKey, PublicKey}

import scala.concurrent.duration.{DAYS, FiniteDuration, MILLISECONDS}
import scala.util.{Failure, Success, Try}

import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import pdi.jwt.exceptions.JwtExpirationException

object Ed25519JwtController:
  def make(
      privateKey: PrivateKey,
      publicKey: PublicKey
  ): JwtController[PrivateKey, PublicKey] = new:
    val algo = JwtAlgorithm.Ed25519

    override def encodeContentIntoJwt(content: String)(using clock: Clock[IO]): IO[String] =
      for
        currTime <- clock.realTime
        claims = JwtClaim(
          content = content,
          issuer = Some("ergo-index.fund"),
          subject = None,
          audience = Some(Set("ergo-index.fund")),
          expiration = Some((currTime + FiniteDuration(1, DAYS)).toMillis),
          notBefore = None,
          issuedAt = Some(currTime.toMillis)
        )
        jwt <- IO(JwtCirce.encode(claims, privateKey, algo))
      yield jwt

    override def decodeContentFromJwt(jwt: String): Either[E, String] =
      JwtCirce.decode(jwt, publicKey, Seq(algo)) match
        case Success(claims: JwtClaim)          => Right(claims.content)
        case Failure(_: JwtExpirationException) => Left(E.TokenExpired)
        case Failure(_)                         => Left(E.TokenInvalid)
