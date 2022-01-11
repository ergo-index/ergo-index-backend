package fund.ergoindex.backend
package main

import cats.effect.IO

import fund.ergoindex.backend.auth.{
  AuthUserBoundary,
  AuthUserEntity,
  RedisAuthUserController,
  RedisAuthUserEntityGateway
}
import fund.ergoindex.backend.http4s.{AuthMiddleware, HttpAppMaker, HttpBoundary, HttpController}
import fund.ergoindex.backend.http4s.auth.{
  HttpBoundary => AuthHttpBoundary,
  HttpController => AuthHttpController
}
import fund.ergoindex.backend.http4s.user.{
  HttpBoundary => UserHttpBoundary,
  HttpController => UserHttpController
}
import fund.ergoindex.backend.jwt.{Ed25519JwtController, JwtBoundary}

import org.http4s.HttpApp

import java.security.{PrivateKey, PublicKey}

import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.effect.Log.Stdout.*

object DependencyGraph:
  def make(
      privateKey: PrivateKey,
      publicKey: PublicKey,
      redis: RedisCommands[IO, String, String]
  ): IO[HttpApp[IO]] = IO {
    val authUserEntityGateway = RedisAuthUserEntityGateway.make(redis)

    val jwtController      = Ed25519JwtController.make(privateKey, publicKey)
    val authUserController = RedisAuthUserController.make(authUserEntityGateway)
    val jwtBoundary        = JwtBoundary.make(jwtController)
    val authUserBoundary   = AuthUserBoundary.make(authUserController)

    val authMiddleware     = AuthMiddleware.make(jwtBoundary)
    val authHttpController = AuthHttpController.make(jwtBoundary, authUserBoundary)
    val userHttpController = UserHttpController.make(authMiddleware)
    val authHttpBoundary   = AuthHttpBoundary.make(authHttpController)
    val userHttpBoundary   = UserHttpBoundary.make(userHttpController)

    HttpAppMaker.make(authHttpBoundary, userHttpBoundary)
  }
