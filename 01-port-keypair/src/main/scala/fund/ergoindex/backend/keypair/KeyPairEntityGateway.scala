package fund.ergoindex.backend
package keypair

import cats.data.EitherT
import cats.effect.IO

/** put() will overwrite a KeyPairEntity if one already exists in the storage.
  */
trait KeyPairEntityGateway[KeyPairEntity]:
  def get(): EitherT[IO, E, KeyPairEntity]
  def put(keyPair: KeyPairEntity): EitherT[IO, E, Unit]
