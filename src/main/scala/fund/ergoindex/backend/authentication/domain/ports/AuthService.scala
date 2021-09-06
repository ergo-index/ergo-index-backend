package fund.ergoindex.backend
package authentication.domain.ports

import fund.ergoindex.backend.authentication.domain.models.{AuthToken, AuthUser}

/** Core service for user authentication.
  */
trait AuthService:
  /** @param password the password that the user is attempting to authenticate with
    * @param authUser the authUser to authenticate the password against
    * @return a JWT (String) or error (String)
    */
  def authenticate(password: String, authUser: AuthUser): Either[String, String]
