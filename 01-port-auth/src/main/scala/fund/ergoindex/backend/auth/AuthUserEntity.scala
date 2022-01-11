package fund.ergoindex.backend
package auth

final case class AuthUserEntity(email: String, passwordHash: String)
