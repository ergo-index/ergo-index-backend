val catsVersion       = "2.6.1"
val catsEffectVersion = "3.1.1"
val circeVersion      = "0.14.1"
val firebaseVersion   = "8.1.0"
val fs2Version        = "3.1.1"
val http4sVersion     = "1.0.0-M23"

ThisBuild / organization := "fund.ergo-index"
ThisBuild / scalaVersion := "3.0.0"
ThisBuild / version      := "0.0.1-SNAPSHOT"

// Only build sub-projects if they aren't don't have .class files generated
ThisBuild / trackInternalDependencies := TrackLevel.TrackIfMissing
ThisBuild / exportJars                := true

// Set merge strategy for duplicate files when generating fat JAR
ThisBuild / assemblyMergeStrategy := {
  case x if x.contains("io.netty.versions.properties") => MergeStrategy.discard
  case x if x.contains("native-image.properties")      => MergeStrategy.discard
  case x if x.contains("reflection-config.json")       => MergeStrategy.discard
  case x if x.contains("module-info.class")            => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

lazy val root =
  project
    .in(file("."))
    .aggregate(
      firebase,
      `delivery-http4s`,
      main
    )
    .dependsOn(
      firebase,
      `delivery-http4s`,
      main
    )
    // Fat JAR configuration using sbt-assembly plugin
    .settings(assembly / mainClass := Some("fund.ergoindex.backend.main.Main"))
    .settings(assembly / assemblyJarName := "ergoindex-backend-0.0.1.jar")

lazy val firebase = project
  .in(file("02-firebase"))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"      %% "cats-core"      % catsVersion,
      "org.typelevel"      %% "cats-effect"    % catsEffectVersion,
      "com.google.firebase" % "firebase-admin" % firebaseVersion
    )
  )

lazy val `delivery-http4s` = project
  .in(file("02-delivery-http4s"))
  .dependsOn(firebase % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      "io.circe"           %% "circe-generic"       % circeVersion,
      "org.typelevel"      %% "cats-core"           % catsVersion,
      "org.typelevel"      %% "cats-effect"         % catsEffectVersion,
      "org.http4s"         %% "http4s-circe"        % http4sVersion,
      "org.http4s"         %% "http4s-dsl"          % http4sVersion,
      "org.http4s"         %% "http4s-blaze-server" % http4sVersion,
      "com.google.firebase" % "firebase-admin"      % firebaseVersion
    )
  )

lazy val main = project
  .in(file("03-main"))
  .dependsOn(firebase % "compile->compile;test->test")
  .dependsOn(`delivery-http4s` % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"      %% "cats-core"           % catsVersion,
      "org.typelevel"      %% "cats-effect"         % catsEffectVersion,
      "org.http4s"         %% "http4s-circe"        % http4sVersion,
      "org.http4s"         %% "http4s-dsl"          % http4sVersion,
      "org.http4s"         %% "http4s-blaze-server" % http4sVersion,
      "org.slf4j"           % "slf4j-simple"        % "1.7.31",
      "com.google.firebase" % "firebase-admin"      % firebaseVersion
    )
  )
  // Fat JAR configuration using sbt-assembly plugin
  .settings(assembly / mainClass := Some("fund.ergoindex.backend.main.Main"))
  .settings(assembly / assemblyJarName := "ergoindex-backend-0.0.1.jar")
