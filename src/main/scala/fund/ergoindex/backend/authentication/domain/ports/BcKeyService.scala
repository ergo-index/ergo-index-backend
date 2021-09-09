package fund.ergoindex.backend
package authentication.domain.ports

import cats.effect.IO

import java.security.{KeyPair, KeyPairGenerator}

import scala.util.Try

/** The result of trying to add a security provider.
  */
enum SecurityProviderResult:
  case Success, Already_Present

/** Service for generating [[java.security.KeyPair]]s using BouncyCastle.
  */
trait BcKeyService:
  /** Adds BouncyCastle to the list of security providers.
    * This is needed to generate [[java.security.KeyPair]]s.
    */
  def addBcProvider(): IO[Either[Throwable, SecurityProviderResult]]

  def getGenerator(): IO[Either[Throwable, KeyPairGenerator]]

  /** @return a newly-generated KeyPair
    */
  def generate(generator: KeyPairGenerator): IO[KeyPair]
