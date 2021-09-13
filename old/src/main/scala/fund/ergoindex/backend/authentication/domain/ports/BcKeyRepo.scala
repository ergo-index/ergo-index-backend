package fund.ergoindex.backend
package authentication.domain.ports

import cats.data.EitherT
import cats.effect.IO

import java.security.{KeyPair, KeyPairGenerator}

/** Errors that could occur when interacting with the repo.
  */
enum BcKeyRepoErr:
  case WrongAlgorithm, DatabaseDown, UnknownError, InvalidPrivateKey, InvalidPublicKey

/** Repository for saving and loading a single [[java.security.KeyPair]] using BouncyCastle.
  * Does not support multiple KeyPairs.
  */
trait BcKeyRepo:

  /** @return the KeyPair that is saved in the repository, or a BcKeyRepoErr
    */
  def get(): EitherT[IO, BcKeyRepoErr, KeyPair]

  /** Saves the given KeyPair in the repository, replacing the previous-saved KeyPair if one already exists.
    *
    * @param keyPair the KeyPair to save
    * @return Unit if the KeyPair was successfully saved, or a BcKeyRepoErr if there was an error
    */
  def put(keyPair: KeyPair): EitherT[IO, BcKeyRepoErr, Unit]

  /** Tries to get a KeyPair that is saved in the repo, and generates/saves a new one if none already exists.
    *
    * @param keyPairGenerator the generator to be used if a new key needs to be generated
    * @param generate: the function that will use keyPairGenerator to generate a new key
    * @return the retrieved or generated key, or a BcKeyRepoErr if there was an error
    */
  def getOrGenerate(
      keyPairGenerator: KeyPairGenerator,
      generate: (KeyPairGenerator) => IO[KeyPair]
  ): EitherT[IO, BcKeyRepoErr, KeyPair]
