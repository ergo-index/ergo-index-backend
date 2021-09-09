package fund.ergoindex.backend

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits.*

import fund.ergoindex.backend.authentication.domain.ports
import fund.ergoindex.backend.authentication.domain.ports.{
  AuthMiddleware,
  AuthService,
  AuthUserRepo,
  BcKeyRepo,
  BcKeyRepoErr,
  BcKeyService,
  JwtService,
  SecurityProviderResult
}
import fund.ergoindex.backend.authentication.infrastructure.adapters.{
  DefaultAuthMiddleware,
  DefaultAuthService,
  Ed25519BcKeyService,
  Ed25519FileBcKeyRepo,
  Ed25519JwtService,
  RedisAuthUserRepo
}
import fund.ergoindex.backend.authentication.infrastructure.api.AuthRoutes
import fund.ergoindex.backend.user.infrastructure.api.UserRoutes

import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits.*

import java.net.InetSocketAddress
import java.security.{KeyPair, KeyPairGenerator}

import scala.util.{Failure, Success, Try}

object Server extends IOApp.Simple:

  def run: IO[Unit] = serverProgram.use(_ => IO.never)

  lazy val serverProgram =
    // Instantiate repo and service for JWT keys using BouncyCastle and Ed25519
    val keyPairRepo: BcKeyRepo       = Ed25519FileBcKeyRepo()
    val keyPairService: BcKeyService = Ed25519BcKeyService()

    for
      // Add BouncyCastle security provider for JWTs, or abort on error
      addBCResult <- Resource.eval(keyPairService.addBcProvider())
      _ <- Resource.eval(
        addBCResult.fold(
          IO.raiseError,
          _ match
            case SecurityProviderResult.Success =>
              IO.println("Added BouncyCastle security provider")
            case SecurityProviderResult.Already_Present =>
              IO.println("BouncyCastle security provider is already present -- skipping adding it")
        )
      )

      // Get saved private/public key pair, or generate a new one (abort on error)
      keyPairGeneratorResult <- Resource.eval(keyPairService.getGenerator())
      keyPairGenerator <- Resource.eval(
        keyPairGeneratorResult.fold(
          IO.raiseError,
          keyPairGenerator => IO(keyPairGenerator)
        )
      )
      keyPair <- Resource.eval(
        keyPairRepo
          .get()
          .flatMap(_ match
            case Right(keyPair) => IO(keyPair)
            case Left(BcKeyRepoErr.InvalidPrivateKey) | Left(BcKeyRepoErr.InvalidPublicKey) =>
              for
                _         <- IO.println("Error reading key pair. Trying to generate a new one...")
                keyPair   <- keyPairService.generate(keyPairGenerator)
                putResult <- keyPairRepo.put(keyPair)
                _ <- putResult.fold(
                  err => IO.raiseError(RuntimeException("Error saving key: " + err)),
                  _ => IO(())
                )
                _ <- IO.println("Created and saved new private/public key pair")
              yield keyPair
            case Left(BcKeyRepoErr.DatabaseDown) =>
              IO.raiseError(
                RuntimeException("Unable to read JWT keys because the database is down")
              )
            case Left(BcKeyRepoErr.WrongAlgorithm) =>
              IO.raiseError(
                RuntimeException(
                  "Unable to read JWT keys because a different algorithm from the one specified now was given when they were encrypted"
                )
              )
            case Left(BcKeyRepoErr.UnknownError) =>
              IO.raiseError(
                RuntimeException("Unable to read JWT keys because an unknown error occurred")
              )
          )
      )

      // Instantiate authentication classes
      jwtService: JwtService         = Ed25519JwtService(keyPair)
      authMiddleware: AuthMiddleware = DefaultAuthMiddleware()
      authService: AuthService       = DefaultAuthService(jwtService)
      authUserRepo: AuthUserRepo     = RedisAuthUserRepo()

      // Build the server!
      port = 8080
      host = "0.0.0.0"
      server <- BlazeServerBuilder[IO](runtime.compute)
        .bindSocketAddress(InetSocketAddress(host, port))
        .withHttpApp(getAllRoutes(authService, jwtService, authMiddleware, authUserRepo).orNotFound)
        .resource
    yield server

  private def getAllRoutes(
      authService: AuthService,
      jwtService: JwtService,
      authMiddleware: AuthMiddleware,
      authUserRepo: AuthUserRepo
  ) =
    val authedRoutes = authMiddleware.toAuthedRoutes(UserRoutes.routes, jwtService)
    val publicRoutes = AuthRoutes(authService, authUserRepo).routes
    val allRoutes    = authedRoutes <+> publicRoutes
    allRoutes
