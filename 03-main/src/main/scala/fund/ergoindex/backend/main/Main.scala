package fund.ergoindex.backend
package main

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  override def run: IO[Unit] = Program.make(runtime.compute)
