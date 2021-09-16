package fund.ergoindex.backend
package main

import cats.data.EitherT
import cats.effect.IO

import fund.ergoindex.backend.http4s.HttpServer
import fund.ergoindex.backend.keypair.{
  BcEd25519KeyPairController,
  BcEd25519KeyPairEntityGateway,
  KeyPairBoundary
}
import fund.ergoindex.backend.keypair.{E => KeyPairE}

import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object Program:
  type E = Throwable | KeyPairE

  def make(executionContext: ExecutionContext): IO[Unit] =
    for
      keyPairBoundary <- IO(
        KeyPairBoundary.make(BcEd25519KeyPairController.make(BcEd25519KeyPairEntityGateway.make()))
      )
      keyPairOrErr <- keyPairBoundary.getOrGenerate().value
      keyPair      <- keyPairOrErr.fold(throwKeyPairE, IO.apply)
      httpApp      <- DependencyGraph.make(keyPair.getPrivate, keyPair.getPublic)
      server       <- HttpServer.make[E](httpApp, executionContext, "0.0.0.0", 8080)
      _            <- server.serve
    yield ()

  // TODO: Put these strings into the actual error types and then just do IO.raiseError(RuntimeException(e.msg))
  def throwKeyPairE[KeyPair](e: KeyPairE): IO[KeyPair] =
    e match
      case KeyPairE.WrongAlgorithm =>
        IO.raiseError(
          RuntimeException(
            "Unable to read keys used to read/write JWTs because a different algorithm that was used to create the keys is not the same algorithm being used currently"
          )
        )
      case KeyPairE.DatabaseDown =>
        IO.raiseError(
          RuntimeException(
            "Unable to connect to the database to retrieve keys used to read/write JWTs"
          )
        )
      case KeyPairE.InvalidPrivateKey | KeyPairE.InvalidPublicKey | KeyPairE.UnknownError =>
        IO.raiseError(RuntimeException("Error with keys used to read/write JWTs: " + e))
