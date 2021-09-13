package fund.ergoindex.backend
package authentication.infrastructure.adapters

import cats.data.EitherT
import cats.effect.IO

import fund.ergoindex.backend.authentication.domain.ports.{
  BcKeyRepo,
  BcKeyService,
  SecurityProviderResult
}

import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.security.{
  KeyFactory,
  KeyPair,
  KeyPairGenerator,
  NoSuchAlgorithmException,
  PrivateKey,
  PublicKey,
  Security
}

import scala.util.Try

/** Implementation of BouncyCastleKeyPairService for KeyPairs with an EdDSA signature scheme using
  * SHA-512 and Curve25519 (AKA Ed25519).
  *
  * Uses BouncyCastle, so addBouncyCastleProvider() must be run before generating KeyPairs.
  */
class Ed25519BcKeyService extends BcKeyService:

  override def addBcProvider(): EitherT[IO, Throwable, SecurityProviderResult] =
    EitherT(Option(Security.getProvider("BC")) match
      case Some(_) => IO(Right(SecurityProviderResult.Already_Present))
      case None =>
        Try(Security.addProvider(BouncyCastleProvider())).fold(
          err => IO(Left(err)),
          _ => IO(Right(SecurityProviderResult.Success))
        )
    )

  override def getGenerator(): EitherT[IO, Throwable, KeyPairGenerator] =
    EitherT(IO {
      try
        val keyPairGenerator = KeyPairGenerator.getInstance("Ed25519", "BC")
        Right(keyPairGenerator)
      catch
        case err: IllegalArgumentException  => Left(err)
        case err2: NoSuchAlgorithmException => Left(err2)
        case err3: NullPointerException     => Left(err3)
      finally Left(RuntimeException("Unable to get the KeyPairGenerator instance"))
    })

  override def generate(generator: KeyPairGenerator): IO[KeyPair] =
    IO(generator.generateKeyPair())
