val catsVersion       = "2.6.1"
val catsEffectVersion = "3.1.1"
val circeVersion      = "0.14.1"
val fs2Version        = "3.1.1"
val http4sVersion     = "1.0.0-M23"
val jwtVersion        = "8.0.2"
val zioVersion        = "2.0.0-M2"

lazy val root = (project in file("."))
  .settings(
    organization := "fund.ergo-index",
    name         := "ergo-index-backend",
    version      := "0.0.1-SNAPSHOT",
    scalaVersion := "3.0.0",
    libraryDependencies ++= Seq(
      "co.fs2"               %% "fs2-core"            % fs2Version,
      "co.fs2"               %% "fs2-io"              % fs2Version,
      "com.github.jwt-scala" %% "jwt-core"            % jwtVersion,
      "com.github.jwt-scala" %% "jwt-circe"           % jwtVersion,
      "dev.zio"              %% "zio"                 % zioVersion,
      "dev.zio"              %% "zio-streams"         % zioVersion,
      "io.circe"             %% "circe-generic"       % circeVersion,
      "org.http4s"           %% "http4s-circe"        % http4sVersion,
      "org.http4s"           %% "http4s-dsl"          % http4sVersion,
      "org.http4s"           %% "http4s-blaze-server" % http4sVersion,
      "org.http4s"           %% "http4s-blaze-client" % http4sVersion,
      "org.typelevel"        %% "cats-core"           % catsVersion,
      "org.typelevel"        %% "cats-effect"         % catsEffectVersion,
      "org.slf4j"             % "slf4j-simple"        % "1.7.31",
      "org.jooq"              % "jooq"                % "3.14.12",
      "org.bouncycastle"      % "bcprov-jdk15on"      % "1.69",
      "org.bouncycastle"      % "bcpkix-jdk15on"      % "1.69"
    )
  )
