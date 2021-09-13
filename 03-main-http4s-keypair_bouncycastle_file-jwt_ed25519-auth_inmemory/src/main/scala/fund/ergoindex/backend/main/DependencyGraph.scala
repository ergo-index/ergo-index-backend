package fund.ergoindex.backend
package main

import cats.effect.IO

import fund.ergoindex.backend.auth.{
  AuthUserBoundary,
  AuthUserEntity,
  InMemoryAuthUserController,
  InMemoryAuthUserEntityGateway
}
import fund.ergoindex.backend.http4s.{Http4sBoundary, Http4sController}
import fund.ergoindex.backend.jwt.{Ed25519JwtController, JwtBoundary}

import java.security.{PrivateKey, PublicKey}

object DependencyGraph:
  def make(privateKey: PrivateKey, publicKey: PublicKey): IO[Http4sBoundary] = IO {
    val authUserEntityGateway = InMemoryAuthUserEntityGateway.make()

    val jwtController      = Ed25519JwtController.make(privateKey, publicKey)
    val authUserController = InMemoryAuthUserController.make(authUserEntityGateway)

    val jwtBoundary      = JwtBoundary.make(jwtController)
    val authUserBoundary = AuthUserBoundary.make(authUserController)

    Http4sBoundary.make(Http4sController.make(jwtBoundary, authUserBoundary))
  }
