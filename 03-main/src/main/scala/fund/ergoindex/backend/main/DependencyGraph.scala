package fund.ergoindex.backend
package main

import cats.effect.IO

import com.google.firebase.FirebaseApp

import fund.ergoindex.backend.firebase.FirebaseBoundary
import fund.ergoindex.backend.http4s.{
  FirebaseAuthMiddleware,
  HttpAppMaker,
  HttpBoundary,
  HttpController
}
import fund.ergoindex.backend.http4s.user.{
  HttpBoundary => UserHttpBoundary,
  HttpController => UserHttpController
}

import org.http4s.HttpApp

object DependencyGraph:
  def make(
      firebaseApp: FirebaseApp,
      firebaseBoundary: FirebaseBoundary
  ): IO[HttpApp[IO]] = IO {
    val firebaseAuthMiddleware = FirebaseAuthMiddleware.make(firebaseApp, firebaseBoundary)
    val userHttpController     = UserHttpController.make(firebaseAuthMiddleware)
    val userHttpBoundary       = UserHttpBoundary.make(userHttpController)

    HttpAppMaker.make(userHttpBoundary)
  }
