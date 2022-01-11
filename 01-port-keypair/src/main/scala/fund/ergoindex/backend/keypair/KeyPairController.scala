package fund.ergoindex.backend
package keypair

import cats.data.EitherT
import cats.effect.IO

trait KeyPairController[KeyPairEntity]:
  def generate(): EitherT[IO, E, KeyPairEntity]
  def getOrGenerate(): EitherT[IO, E, KeyPairEntity]
