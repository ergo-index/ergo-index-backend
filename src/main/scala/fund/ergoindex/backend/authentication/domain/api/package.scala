package fund.ergoindex.backend
package authentication.domain

import cats.effect.IO

import io.circe.generic.auto.deriveDecoder

import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

package object api:
  implicit val logInRequestDecoder: EntityDecoder[IO, LogInRequest]   = jsonOf[IO, LogInRequest]
  implicit val signUpRequestDecoder: EntityDecoder[IO, SignUpRequest] = jsonOf[IO, SignUpRequest]
