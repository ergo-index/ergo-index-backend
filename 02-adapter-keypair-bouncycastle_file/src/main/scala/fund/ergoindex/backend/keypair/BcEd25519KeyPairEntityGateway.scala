package fund.ergoindex.backend
package keypair

import cats.data.EitherT
import cats.effect.IO

import java.security.{Key, KeyFactory, KeyPair, PrivateKey, PublicKey}
import java.security.KeyPair
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.util.Base64

import fs2.{text, Stream}
import fs2.io.file.{Files, Path}

/** Provides flat file storage for a single KeyPair with an
  * EdDSA signature scheme using SHA-512 and Curve25519 (AKA Ed25519).
  */
object BcEd25519KeyPairEntityGateway:
  def make(): KeyPairEntityGateway[KeyPair] = new:

    val keyAlgo     = "Ed25519"
    val keyProvider = "BC"

    override def get(): EitherT[IO, E, KeyPair] =
      for
        privateKey <- readKeyFromPath[PrivateKey]("jwt_private_key", stringToPrivateKey)
        publicKey  <- readKeyFromPath[PublicKey]("jwt_public_key", stringToPublicKey)
      yield KeyPair(publicKey, privateKey)

    override def put(keyPair: KeyPair): EitherT[IO, E, Unit] =
      for
        privateKey <- keyToString(keyPair.getPrivate)
        publicKey  <- keyToString(keyPair.getPublic)
        _          <- writeKeyToFile(privateKey, "jwt_private_key")
        _          <- writeKeyToFile(publicKey, "jwt_public_key")
      yield ()

    def keyToString(key: Key): EitherT[IO, E, String] =
      EitherT(IO {
        if key.getAlgorithm().equals("Ed25519") then
          Right(Base64.getEncoder().encodeToString(key.getEncoded()))
        else Left(E.WrongAlgorithm)
      })

    def stringToPrivateKey(key: String): EitherT[IO, E, PrivateKey] =
      EitherT(IO {
        try
          val spec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(key))
          Right(KeyFactory.getInstance(keyAlgo, keyProvider).generatePrivate(spec))
        catch _ => Left(E.InvalidPrivateKey)
      })

    def stringToPublicKey(key: String): EitherT[IO, E, PublicKey] =
      EitherT(IO {
        try
          val spec = X509EncodedKeySpec(Base64.getDecoder().decode(key))
          Right(KeyFactory.getInstance(keyAlgo, keyProvider).generatePublic(spec))
        catch _ => Left(E.InvalidPublicKey)
      })

    def verifyKeyPathExists(pathString: String): EitherT[IO, E, Path] =
      EitherT(IO {
        try
          val path = Path(pathString)
          if path.toNioPath.toFile.exists then Right(path)
          else Left(E.InvalidPublicKey)
        catch _ => Left(E.UnknownError)
      })

    def readKeyFromPath[KeyType <: Key](
        pathName: String,
        parseFunction: String => EitherT[IO, E, KeyType]
    ): EitherT[IO, E, KeyType] =
      for
        path <- verifyKeyPathExists(pathName)
        keyStringIo = Files[IO]
          .readAll(path)
          .through(text.utf8.decode)
          .compile
          .string
        keyString <- EitherT.right[E](keyStringIo)
        key       <- parseFunction(keyString)
      yield key

    def writeKeyToFile(privateKey: String, pathName: String): EitherT[IO, E, Unit] =
      for
        _ <- EitherT.right[E](
          Stream(privateKey)
            .through(text.utf8.encode)
            .through(Files[IO].writeAll(Path(pathName)))
            .compile
            .drain
        )
      yield ()
