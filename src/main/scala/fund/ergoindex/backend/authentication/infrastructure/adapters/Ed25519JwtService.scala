package fund.ergoindex.backend
package authentication.infrastructure.adapters

import cats.effect.{IO, Resource}

import fund.ergoindex.backend.authentication.domain.models.AuthToken
import fund.ergoindex.backend.authentication.domain.ports.JwtService

import io.circe.*
import io.circe.generic.*
import io.circe.generic.auto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.*

import org.http4s.EntityDecoder

import java.security.KeyPair

import scala.util.{Failure, Success, Try}

import pdi.jwt.{JwtAlgorithm, JwtBase64, JwtCirce, JwtClaim}

/** Service for encoding and decoding JWTs that use an
  * EdDSA signature scheme using SHA-512 and Curve25519 (AKA Ed25519).
  *
  * @param keyPair the pair of private and public keys for encoding and decoding JWTs
  */
case class Ed25519JwtService(keyPair: KeyPair) extends JwtService:
  val algo = JwtAlgorithm.Ed25519

  def encodeToken(token: AuthToken): String =
    val claim = JwtClaim(
      // TODO: fill out a claim and encode that instead of just the token
    )
    JwtCirce.encode(token.asJson, keyPair.getPrivate, algo)

  def decodeToken(token: String): Option[AuthToken] =
    val decoded: Try[Json] = JwtCirce.decodeJson(token, keyPair.getPublic, Seq(algo))
    decoded match
      case Success(jwtClaim) =>
        println("successfully decoded: " + jwtClaim) // TODO: Remove debug
        jwtClaim.as[AuthToken].toOption
      case Failure(_) =>
        println("failed to decode token") // TODO: Remove debug
        None
    None
