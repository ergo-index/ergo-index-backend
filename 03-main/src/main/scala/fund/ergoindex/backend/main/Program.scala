package fund.ergoindex.backend
package main

import cats.effect.IO

import fund.ergoindex.backend.firebase.FirebaseBoundary
import fund.ergoindex.backend.http4s.HttpServer

import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object Program:
  def make(executionContext: ExecutionContext): IO[Unit] =
    for
      firebaseBoundary <- IO(FirebaseBoundary.make())
      firebaseApp      <- firebaseBoundary.init()
      httpApp          <- DependencyGraph.make(firebaseApp, firebaseBoundary)
      server           <- HttpServer.make(httpApp, executionContext, "0.0.0.0", 8080)
      _                <- server.serve
    yield ()
