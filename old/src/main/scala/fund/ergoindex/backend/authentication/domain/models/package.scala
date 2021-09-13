package fund.ergoindex.backend
package authentication.domain

import cats.effect.IO

import io.circe.generic.auto.deriveDecoder

import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

package object models:
  implicit val authUserDecoder: EntityDecoder[IO, AuthUser] = jsonOf[IO, AuthUser]
