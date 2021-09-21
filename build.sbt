val catsVersion       = "2.6.1"
val catsEffectVersion = "3.1.1"
val circeVersion      = "0.14.1"
val fs2Version        = "3.1.1"
val http4sVersion     = "1.0.0-M23"
val jwtVersion        = "8.0.2"
val redis4catsVersion = "1.0.0"

ThisBuild / organization := "fund.ergo-index"
ThisBuild / scalaVersion := "3.0.0"
ThisBuild / version      := "0.0.1-SNAPSHOT"

// Only build sub-projects if they aren't don't have .class files generated
ThisBuild / trackInternalDependencies := TrackLevel.TrackIfMissing
ThisBuild / exportJars                := true

// Set merge strategy for duplicate files when generating fat JAR
ThisBuild / assemblyMergeStrategy := {
  case x if x.contains("io.netty.versions.properties") => MergeStrategy.discard
  case x if x.contains("module-info.class")            => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

lazy val root =
  project
    .in(file("."))
    .aggregate(
      `port-keypair`,
      `adapter-keypair-bouncycastle_file`,
      `port-jwt`,
      `adapter-jwt-ed25519`,
      `port-auth`,
      `persistence-auth-redis`,
      `delivery-http4s`,
      main
    )
    .dependsOn(
      `port-keypair`,
      `adapter-keypair-bouncycastle_file`,
      `port-jwt`,
      `adapter-jwt-ed25519`,
      `port-auth`,
      `persistence-auth-redis`,
      `delivery-http4s`,
      main
    )
    // Fat JAR configuration using sbt-assembly plugin
    .settings(assembly / mainClass := Some("fund.ergoindex.backend.main.Main"))
    .settings(assembly / assemblyJarName := "ergoindex-backend-0.0.1.jar")

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

lazy val `persistence-auth-redis` = project
  .in(file("02-persistence-auth-redis"))
  .dependsOn(`port-auth` % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      "dev.profunktor" %% "redis4cats-effects"  % redis4catsVersion,
      "dev.profunktor" %% "redis4cats-log4cats" % redis4catsVersion,
      "org.typelevel"  %% "cats-core"           % catsVersion,
      "org.typelevel"  %% "cats-effect"         % catsEffectVersion
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

lazy val main = project
  .in(file("03-main"))
  .dependsOn(`port-auth` % "compile->compile;test->test")
  .dependsOn(`port-jwt` % "compile->compile;test->test")
  .dependsOn(`port-keypair` % "compile->compile;test->test")
  .dependsOn(`delivery-http4s` % "compile->compile;test->test")
  .dependsOn(`adapter-keypair-bouncycastle_file` % "compile->compile;test->test")
  .dependsOn(`adapter-jwt-ed25519` % "compile->compile;test->test")
  .dependsOn(`persistence-auth-redis` % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      "dev.profunktor" %% "redis4cats-effects"  % redis4catsVersion,
      "dev.profunktor" %% "redis4cats-log4cats" % redis4catsVersion,
      "org.typelevel"  %% "cats-core"           % catsVersion,
      "org.typelevel"  %% "cats-effect"         % catsEffectVersion,
      "org.http4s"     %% "http4s-circe"        % http4sVersion,
      "org.http4s"     %% "http4s-dsl"          % http4sVersion,
      "org.http4s"     %% "http4s-blaze-server" % http4sVersion,
      "org.slf4j"       % "slf4j-simple"        % "1.7.31"
    )
  )
  // Fat JAR configuration using sbt-assembly plugin
  .settings(assembly / mainClass := Some("fund.ergoindex.backend.main.Main"))
  .settings(assembly / assemblyJarName := "ergoindex-backend-0.0.1.jar")
