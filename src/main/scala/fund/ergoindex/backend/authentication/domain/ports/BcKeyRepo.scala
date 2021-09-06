package fund.ergoindex.backend
package authentication.domain.ports

import cats.effect.IO

import java.security.KeyPair

import scala.util.Try

/** Errors that could occur when interacting with the repo.
  */
enum BcKeyRepoErr:
  case WrongAlgorithm, DatabaseDown, UnknownError, InvalidPrivateKey, InvalidPublicKey

/** Repository for saving and loading a single [[java.security.KeyPair]] using BouncyCastle.
  * Does not support multiple KeyPairs.
  */
trait BcKeyRepo:

  /** @return the KeyPair that is saved in the repository, or an error
    */
  def get(): IO[Either[BcKeyRepoErr, KeyPair]]

  /** Saves the given KeyPair in the repository, replacing the previous-saved KeyPair if one
    * already exists.
    *
    * @param keyPair the KeyPair to save
    * @return Unit if the KeyPair was successfully saved, or a BcKeyRepoErr if there was an error
    */
  def put(keyPair: KeyPair): IO[Either[BcKeyRepoErr, Unit]]
