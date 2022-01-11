package fund.ergoindex.backend
package keypair

import cats.data.EitherT
import cats.effect.IO

trait KeyPairBoundary[KeyPairEntity]:
  def generate(): EitherT[IO, E, KeyPairEntity]
  def getOrGenerate(): EitherT[IO, E, KeyPairEntity]

object KeyPairBoundary:
  def make[KeyPairEntity](
      controller: KeyPairController[KeyPairEntity]
  ): KeyPairBoundary[KeyPairEntity] = new:
    override def generate(): EitherT[IO, E, KeyPairEntity] = controller.generate()

    override def getOrGenerate(): EitherT[IO, E, KeyPairEntity] = controller.getOrGenerate()
