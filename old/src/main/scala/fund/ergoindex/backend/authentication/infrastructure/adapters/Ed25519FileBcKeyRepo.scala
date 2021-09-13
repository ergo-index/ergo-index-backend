package fund.ergoindex.backend
package authentication.infrastructure.adapters

import cats.data.EitherT
import cats.effect.IO
import cats.implicits.*
import cats.instances.try_

import fund.ergoindex.backend.authentication.domain.ports.{BcKeyRepo, BcKeyRepoErr}

import java.security.{Key, KeyFactory, KeyPair, PrivateKey, PublicKey}
import java.security.{KeyPair, KeyPairGenerator}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.util.Base64

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

  val keyAlgo     = "Ed25519"
  val keyProvider = "BC"

  override def get(): EitherT[IO, BcKeyRepoErr, KeyPair] =
    for
      privateKey <- readKeyFromPath[PrivateKey]("jwt_private_key", stringToPrivateKey)
      publicKey  <- readKeyFromPath[PublicKey]("jwt_public_key", stringToPublicKey)
    yield KeyPair(publicKey, privateKey)

  override def put(keyPair: KeyPair): EitherT[IO, BcKeyRepoErr, Unit] =
    for
      privateKey <- keyToString(keyPair.getPrivate)
      publicKey  <- keyToString(keyPair.getPublic)
      _          <- writeKeyToFile(privateKey, "jwt_private_key")
      _          <- writeKeyToFile(publicKey, "jwt_public_key")
    yield ()

  override def getOrGenerate(
      keyPairGenerator: KeyPairGenerator,
      generate: (KeyPairGenerator) => IO[KeyPair]
  ): EitherT[IO, BcKeyRepoErr, KeyPair] =
    for
      keyPair <- EitherT(
        get().value
          .flatMap(
            _ match
              case Right(keyPair) => IO(Right(keyPair))
              case Left(BcKeyRepoErr.InvalidPrivateKey) | Left(BcKeyRepoErr.InvalidPublicKey) =>
                (for
                  keyPair   <- EitherT.right[BcKeyRepoErr](generate(keyPairGenerator))
                  putResult <- put(keyPair)
                yield keyPair).value
              case Left(err) => IO(Left(err))
          )
      )
    yield keyPair

  def keyToString(key: Key): EitherT[IO, BcKeyRepoErr, String] =
    EitherT(IO {
      if key.getAlgorithm().equals("Ed25519") then
        Right(Base64.getEncoder().encodeToString(key.getEncoded()))
      else Left(BcKeyRepoErr.WrongAlgorithm)
    })

  def stringToPrivateKey(key: String): EitherT[IO, BcKeyRepoErr, PrivateKey] =
    EitherT(IO {
      try
        val spec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(key))
        Right(KeyFactory.getInstance(keyAlgo, keyProvider).generatePrivate(spec))
      catch _ => Left(BcKeyRepoErr.InvalidPrivateKey)
    })

  def stringToPublicKey(key: String): EitherT[IO, BcKeyRepoErr, PublicKey] =
    EitherT(IO {
      try
        val spec = X509EncodedKeySpec(Base64.getDecoder().decode(key))
        Right(KeyFactory.getInstance(keyAlgo, keyProvider).generatePublic(spec))
      catch _ => Left(BcKeyRepoErr.InvalidPublicKey)
    })

  def verifyKeyPathExists(pathString: String): EitherT[IO, BcKeyRepoErr, Path] =
    EitherT(IO {
      try
        val path = Path(pathString)
        if path.toNioPath.toFile.exists then Right(path)
        else Left(BcKeyRepoErr.InvalidPublicKey)
      catch _ => Left(BcKeyRepoErr.UnknownError)
    })

  def readKeyFromPath[KeyType <: Key](
      pathName: String,
      parseFunction: String => EitherT[IO, BcKeyRepoErr, KeyType]
  ): EitherT[IO, BcKeyRepoErr, KeyType] =
    for
      path <- verifyKeyPathExists(pathName)
      keyStringIo = Files[IO]
        .readAll(path)
        .through(text.utf8.decode)
        .compile
        .string
      keyString <- EitherT.right[BcKeyRepoErr](keyStringIo)
      key       <- parseFunction(keyString)
    yield key

  def writeKeyToFile(privateKey: String, pathName: String): EitherT[IO, BcKeyRepoErr, Unit] =
    for
      _ <- EitherT.right[BcKeyRepoErr](
        Stream(privateKey)
          .through(text.utf8.encode)
          .through(Files[IO].writeAll(Path(pathName)))
          .compile
          .drain
      )
    yield ()
