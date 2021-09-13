package fund.ergoindex.backend
package authentication.config

object Settings:
  val env = scala.util.Properties.envOrElse("SCALA_ENV", "")

  /* JWT CONFIG */

  // TODO: This can't be known on GitHub once this is in prod.
  //  It should be a one-time, randomly-generated env variable
  val apiJwtSecret = scala.util.Properties.envOrElse("API_JWT_SECRET", "secretKey")
