package fund.ergoindex.backend
package keypair

enum E:
  case WrongAlgorithm, DatabaseDown, UnknownError, InvalidPrivateKey, InvalidPublicKey
  case GeneratorErr(throwable: Throwable)
  case Err(throwable: Throwable)
