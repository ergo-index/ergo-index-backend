package fund.ergoindex.backend
package jwt

import cats.effect.IO

import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax.*

import java.security.{PrivateKey, PublicKey}

import scala.util.{Failure, Success, Try}

import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}

object Ed25519JwtController:
  def make(
      privateKey: PrivateKey,
      publicKey: PublicKey
  ): JwtController[PrivateKey, PublicKey] = new:
    val algo = JwtAlgorithm.Ed25519

    override def encodeContentIntoJwt(content: String): IO[String] = IO {
      val jwt = JwtClaim(
        content = content
        // TODO: Enter the rest of the JWT Claim info like expiration, etc...
      )
      JwtCirce.encode(jwt, privateKey, algo)
    }

    override def decodeContentFromJwt(jwt: String): IO[Option[String]] = IO {
      val decoded: Try[JwtClaim] = JwtCirce.decode(jwt, publicKey, Seq(algo))
      decoded match
        case Success(jwtClaim) =>
          println("successfully decoded: " + jwtClaim) // TODO: Remove debug
          Option(jwtClaim.content)
        case Failure(_) =>
          println("failed to decode token") // TODO: Remove debug
          None
      None
    }
