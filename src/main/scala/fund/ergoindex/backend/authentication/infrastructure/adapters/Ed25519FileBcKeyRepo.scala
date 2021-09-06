package fund.ergoindex.backend
package authentication.infrastructure.adapters

import cats.data.EitherT
import cats.effect.{IO, Resource}
import cats.implicits.*
import cats.instances.try_

import fund.ergoindex.backend.authentication.domain.ports.{BcKeyRepo, BcKeyRepoErr}

import java.io.{BufferedWriter, FileOutputStream, FileWriter}
import java.nio.file.{NoSuchFileException, Paths}
import java.security.{Key, KeyFactory, KeyPair, PrivateKey, PublicKey}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.util.Base64

import scala.util.{Failure, Success, Try}

import fs2.{text, Stream}
import fs2.io.file.{Files, Path}

/** Flat file repository for KeyPairs with an
  * EdDSA signature scheme using SHA-512 and Curve25519 (AKA Ed25519).
  *
  * Uses BouncyCastle, so BouncyCastleKeyPairService#addBouncyCastleProvider()
  * must be run before parsing KeyPairs.
  */
// TODO: Use Redis instead of flat file
class Ed25519FileBcKeyRepo extends BcKeyRepo:

  override def get(): IO[Either[BcKeyRepoErr, KeyPair]] =
    (for {
      privateKey <- readKeyFromPath[PrivateKey]("jwt_private_key", stringToPrivateKey)
      publicKey  <- readKeyFromPath[PublicKey]("jwt_public_key", stringToPublicKey)
    } yield KeyPair(publicKey, privateKey)).value

  override def put(keyPair: KeyPair): IO[Either[BcKeyRepoErr, Unit]] =
    (for {
      privateKey <- EitherT(keyToString(keyPair.getPrivate))
      publicKey  <- EitherT(keyToString(keyPair.getPublic))
      _          <- writeKeyToFile(privateKey, "jwt_private_key")
      _          <- writeKeyToFile(publicKey, "jwt_public_key")
    } yield ()).value

  def keyToString(key: Key): IO[Either[BcKeyRepoErr, String]] =
    IO {
      if key.getAlgorithm().equals("Ed25519") then
        Right(Base64.getEncoder().encodeToString(key.getEncoded()))
      else Left(BcKeyRepoErr.WrongAlgorithm)
    }

  def stringToPrivateKey(
      key: String,
      keyAlgo: String,
      provider: String
  ): IO[Either[BcKeyRepoErr, PrivateKey]] =
    IO {
      try
        val spec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(key))
        Right(KeyFactory.getInstance(keyAlgo, provider).generatePrivate(spec))
      catch case err: Exception => Left(BcKeyRepoErr.InvalidPrivateKey)
      finally Left(BcKeyRepoErr.InvalidPrivateKey)
    }

  def stringToPublicKey(
      key: String,
      keyAlgo: String,
      provider: String
  ): IO[Either[BcKeyRepoErr, PublicKey]] =
    IO {
      try
        val spec = X509EncodedKeySpec(Base64.getDecoder().decode(key))
        Right(KeyFactory.getInstance(keyAlgo, provider).generatePublic(spec))
      catch case err: Exception => Left(BcKeyRepoErr.InvalidPublicKey)
      finally Left(BcKeyRepoErr.InvalidPublicKey)
    }

  def verifyKeyPathExists(pathString: String): IO[Either[BcKeyRepoErr, Path]] =
    IO {
      try
        val path = Path(pathString)
        if path.toNioPath.toFile.exists then Right(path)
        else Left(BcKeyRepoErr.InvalidPublicKey)
      catch case err: Exception => Left(BcKeyRepoErr.UnknownError)
      finally Left(BcKeyRepoErr.UnknownError)
    }

  def readKeyFromPath[KeyType <: Key](
      pathName: String,
      parseFunction: (String, String, String) => IO[Either[BcKeyRepoErr, KeyType]]
  ): EitherT[IO, BcKeyRepoErr, KeyType] =
    for {
      path <- EitherT(verifyKeyPathExists(pathName))
      keyStringIo = Files[IO]
        .readAll(path)
        .through(text.utf8.decode)
        .compile
        .string
      keyString <- EitherT.right[BcKeyRepoErr](keyStringIo)
      parseResult = parseFunction(keyString, "Ed25519", "BC")
      key <- EitherT(parseResult)
    } yield key

  def writeKeyToFile(privateKey: String, pathName: String): EitherT[IO, BcKeyRepoErr, Unit] =
    for {
      _ <- EitherT.right[BcKeyRepoErr](
        Stream(privateKey)
          .through(text.utf8.encode)
          .through(Files[IO].writeAll(Path(pathName)))
          .compile
          .drain
      )
    } yield ()
