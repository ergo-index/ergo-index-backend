val catsVersion       = "2.6.1"
val catsEffectVersion = "3.1.1"
val circeVersion      = "0.14.1"
val fs2Version        = "3.1.1"
val http4sVersion     = "1.0.0-M23"
val jwtVersion        = "8.0.2"

ThisBuild / organization := "fund.ergo-index"
ThisBuild / scalaVersion := "3.0.0"
ThisBuild / version      := "0.0.1-SNAPSHOT"

lazy val `port-keypair` = project
  .in(file("01-port-keypair"))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"   % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion
    )
  )

lazy val `port-jwt` = project
  .in(file("01-port-jwt"))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"   % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion
    )
  )

lazy val `port-auth` = project
  .in(file("01-port-auth"))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"   % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion
    )
  )

lazy val `adapter-keypair-bouncycastle_file` = project
  .in(file("02-adapter-keypair-bouncycastle_file"))
  .dependsOn(`port-keypair` % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"   %% "cats-core"      % catsVersion,
      "org.typelevel"   %% "cats-effect"    % catsEffectVersion,
      "co.fs2"          %% "fs2-core"       % fs2Version,
      "co.fs2"          %% "fs2-io"         % fs2Version,
      "org.bouncycastle" % "bcprov-jdk15on" % "1.69",
      "org.bouncycastle" % "bcpkix-jdk15on" % "1.69"
    )
  )

lazy val `adapter-jwt-ed25519` = project
  .in(file("02-adapter-jwt-ed25519"))
  .dependsOn(`port-jwt` % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      "com.github.jwt-scala" %% "jwt-core"      % jwtVersion,
      "com.github.jwt-scala" %% "jwt-circe"     % jwtVersion,
      "io.circe"             %% "circe-generic" % circeVersion
    )
  )

lazy val `persistence-auth-inmemory` = project
  .in(file("02-persistence-auth-inmemory"))
  .dependsOn(`port-auth` % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"   % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion
    )
  )

lazy val `delivery-http4s` = project
  .in(file("02-delivery-http4s"))
  .dependsOn(`port-auth` % "compile->compile;test->test")
  .dependsOn(`port-jwt` % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      "io.circe"      %% "circe-generic"       % circeVersion,
      "org.typelevel" %% "cats-core"           % catsVersion,
      "org.typelevel" %% "cats-effect"         % catsEffectVersion,
      "org.http4s"    %% "http4s-circe"        % http4sVersion,
      "org.http4s"    %% "http4s-dsl"          % http4sVersion,
      "org.http4s"    %% "http4s-blaze-server" % http4sVersion
    )
  )

lazy val `main-http4s-keypair_bouncycastle_file-jwt_ed25519-auth_inmemory` = project
  .in(file("03-main-http4s-keypair_bouncycastle_file-jwt_ed25519-auth_inmemory"))
  .dependsOn(`port-auth` % "compile->compile;test->test")
  .dependsOn(`port-jwt` % "compile->compile;test->test")
  .dependsOn(`port-keypair` % "compile->compile;test->test")
  .dependsOn(`delivery-http4s` % "compile->compile;test->test")
  .dependsOn(`adapter-keypair-bouncycastle_file` % "compile->compile;test->test")
  .dependsOn(`adapter-jwt-ed25519` % "compile->compile;test->test")
  .dependsOn(`persistence-auth-inmemory` % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"           % catsVersion,
      "org.typelevel" %% "cats-effect"         % catsEffectVersion,
      "org.http4s"    %% "http4s-circe"        % http4sVersion,
      "org.http4s"    %% "http4s-dsl"          % http4sVersion,
      "org.http4s"    %% "http4s-blaze-server" % http4sVersion,
      "org.slf4j"      % "slf4j-simple"        % "1.7.31"
    )
  )

lazy val old = (project in file("old"))
  //.dependsOn(`adapter-keypair-bouncycastle` % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      "co.fs2"               %% "fs2-core"            % fs2Version,
      "co.fs2"               %% "fs2-io"              % fs2Version,
      "com.github.jwt-scala" %% "jwt-core"            % jwtVersion,
      "com.github.jwt-scala" %% "jwt-circe"           % jwtVersion,
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
