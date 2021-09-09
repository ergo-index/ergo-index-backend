package fund.ergoindex.backend
package authentication.infrastructure.adapters

import cats.effect.IO

import fund.ergoindex.backend.authentication.domain.ports.{
  BcKeyRepo,
  BcKeyService,
  SecurityProviderResult
}

import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.io.{BufferedWriter, FileOutputStream, FileWriter}
import java.nio.file.{Files, NoSuchFileException, Paths}
import java.security.{
  KeyFactory,
  KeyPair,
  KeyPairGenerator,
  NoSuchAlgorithmException,
  PrivateKey,
  PublicKey,
  Security
}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.util.Base64

import scala.util.{Failure, Success, Try}

/** Implementation of BouncyCastleKeyPairService for KeyPairs with an EdDSA signature scheme using
  * SHA-512 and Curve25519 (AKA Ed25519).
  *
  * Uses BouncyCastle, so addBouncyCastleProvider() must be run before generating KeyPairs.
  */
class Ed25519BcKeyService extends BcKeyService:

  override def addBcProvider(): IO[Either[Throwable, SecurityProviderResult]] =
    Option(Security.getProvider("BC")) match
      case Some(_) => IO(Right(SecurityProviderResult.Already_Present))
      case None =>
        Try(Security.addProvider(BouncyCastleProvider())).fold(
          err => IO(Left(err)),
          _ => IO(Right(SecurityProviderResult.Success))
        )

  override def getGenerator(): IO[Either[Throwable, KeyPairGenerator]] =
    IO {
      try
        val keyPairGenerator = KeyPairGenerator.getInstance("Ed25519", "BC")
        Right(keyPairGenerator)
      catch
        case err: IllegalArgumentException  => Left(err)
        case err2: NoSuchAlgorithmException => Left(err2)
        case err3: NullPointerException     => Left(err3)
      finally Left(RuntimeException("Unable to get the KeyPairGenerator instance"))
    }

  override def generate(generator: KeyPairGenerator): IO[KeyPair] =
    IO(generator.generateKeyPair())
