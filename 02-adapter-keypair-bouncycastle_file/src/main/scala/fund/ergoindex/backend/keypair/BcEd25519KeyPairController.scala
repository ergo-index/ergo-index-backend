package fund.ergoindex.backend
package keypair

import cats.data.EitherT
import cats.effect.IO

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
  */
object BcEd25519KeyPairController:
  def make(
      keyPairEntityGateway: KeyPairEntityGateway[KeyPair]
  ): KeyPairController[KeyPair] = new:
    override def generate(): EitherT[IO, E, KeyPair] =
      for
        generator <- generator
        keyPair   <- EitherT.rightT[IO, E](generator.generateKeyPair)
      yield keyPair

    override def getOrGenerate(): EitherT[IO, E, KeyPair] =
      for
        _ <- addBcSecurityProvider
        keyPair <- EitherT(
          keyPairEntityGateway
            .get()
            .value
            .flatMap(
              _ match
                case Right(keyPair) => IO(Right(keyPair))
                case Left(E.InvalidPrivateKey) | Left(E.InvalidPublicKey) =>
                  (for
                    keyPair   <- generate()
                    putResult <- keyPairEntityGateway.put(keyPair)
                  yield keyPair).value
                case Left(err) => IO(Left(err))
            )
        )
      yield keyPair

    lazy val addBcSecurityProvider: EitherT[IO, E, Unit] =
      EitherT(Option(Security.getProvider("BC")) match
        case Some(_) => IO(Right(()))
        case None =>
          Try(Security.addProvider(BouncyCastleProvider()))
            .fold(err => IO(Left(E.Err(err))), _ => IO(Right(())))
      )

    lazy val generator: EitherT[IO, E, KeyPairGenerator] =
      for
        _ <- addBcSecurityProvider
        generator <- EitherT(IO {
          try
            val keyPairGenerator = KeyPairGenerator.getInstance("Ed25519", "BC")
            Right(keyPairGenerator)
          catch
            case err: IllegalArgumentException  => Left(E.GeneratorErr(err))
            case err2: NoSuchAlgorithmException => Left(E.GeneratorErr(err2))
            case err3: NullPointerException     => Left(E.GeneratorErr(err3))
          finally
            Left(E.GeneratorErr(RuntimeException("Unable to get the KeyPairGenerator instance")))
        })
      yield generator
