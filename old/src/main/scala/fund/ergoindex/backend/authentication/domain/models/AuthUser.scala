package fund.ergoindex.backend
package authentication.domain.models

/** User for authentication. Separate from regular users.
  */
case class AuthUser(email: String, passwordHash: String)
