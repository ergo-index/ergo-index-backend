package fund.ergoindex.backend

import cats.data.EitherT
import cats.effect.{IO, IOApp, Resource}
import cats.implicits.*

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

object Server extends IOApp.Simple:

  type E = Throwable | BcKeyRepoErr

  lazy val program: EitherT[IO, E, BlazeServerBuilder[IO]] =
    val keyPairRepo    = Ed25519FileBcKeyRepo()
    val keyPairService = Ed25519BcKeyService()
    val authMiddleware = DefaultAuthMiddleware()
    val authUserRepo   = RedisAuthUserRepo()

    for
      _                <- keyPairService.addBcProvider()
      keyPairGenerator <- keyPairService.getGenerator()
      keyPair          <- keyPairRepo.getOrGenerate(keyPairGenerator, keyPairService.generate)

      jwtService  = Ed25519JwtService(keyPair)
      authService = DefaultAuthService(jwtService)
      host        = "0.0.0.0"
      port        = 8080

      server <- EitherT.rightT[IO, E](
        BlazeServerBuilder[IO](runtime.compute)
          .bindSocketAddress(InetSocketAddress(host, port))
          .withHttpApp(
            getAllRoutes(authService, jwtService, authMiddleware, authUserRepo).orNotFound
          )
      )
    yield server

  def run: IO[Unit] = program.value.flatMap {
    case Right(server) => server.serve.compile.drain
    case Left(e: E)    => handleErr(e)
  }

  def getAllRoutes(
      authService: AuthService,
      jwtService: JwtService,
      authMiddleware: AuthMiddleware,
      authUserRepo: AuthUserRepo
  ) =
    val authedRoutes = authMiddleware.toAuthedRoutes(UserRoutes.routes, jwtService)
    val publicRoutes = AuthRoutes(authService, authUserRepo).routes
    val allRoutes    = authedRoutes <+> publicRoutes
    allRoutes

  def handleErr(e: E) = e match
    case (throwable: Throwable) => IO.raiseError(throwable)
    case BcKeyRepoErr.WrongAlgorithm =>
      IO.println(
        "Error: Unable to read JWT keys because a different algorithm from the one specified now was given when they were encrypted"
      )
    case BcKeyRepoErr.DatabaseDown =>
      IO.println("Error: Unable to connect to the database to retrieve JWT keys")
    case BcKeyRepoErr.InvalidPrivateKey | BcKeyRepoErr.InvalidPublicKey |
        BcKeyRepoErr.UnknownError =>
      IO.println("Error: " + e)
