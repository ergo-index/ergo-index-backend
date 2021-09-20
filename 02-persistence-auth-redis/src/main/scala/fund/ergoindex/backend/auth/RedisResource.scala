package fund.ergoindex.backend.auth

import cats.effect.IO
import cats.effect.kernel.Resource

import dev.profunktor.redis4cats.{Redis, RedisCommands}
import dev.profunktor.redis4cats.effect.Log.Stdout.*

object RedisResource:
  def make(): Resource[cats.effect.IO, RedisCommands[IO, String, String]] =
    Redis[IO].utf8("redis://ergo-index-fund-redis")
