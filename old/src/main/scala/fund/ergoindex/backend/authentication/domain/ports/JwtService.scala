package fund.ergoindex.backend
package authentication.domain.ports

import fund.ergoindex.backend.authentication.domain.models.AuthToken

/** Service for manipulating JSON Web Tokens.
  */
trait JwtService:
  /** @param token the token to encode
    * @return a JWT containing the token
    */
  def encodeToken(user: AuthToken): String

  /** @param token the JWT to decode
    * @return an Option.Some containing the AuthToken that was decoded from the JWT, or
    *         Option.None if unsuccessful
    */
  def decodeToken(token: String): Option[AuthToken]
