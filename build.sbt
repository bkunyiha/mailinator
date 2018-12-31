val circeVersion = "0.9.3"
val GuavaVersion = "12.0"
val Http4sVersion = "0.18.21"
val http4sRhoVersion = "0.18.0"
val LogbackVersion = "1.2.3"
val ScalaLoggingVersion = "3.9.0"
val ScalaTestVersion = "3.0.4"
val ScalaMockVersion = "4.1.0"
val ScalaCheckVersion = "1.14.0"
val Specs2Version = "4.1.0"

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    organization := "com.fauna",
    name := "mailinator",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.7",
    scalacOptions ++= Seq(
      "-Xexperimental",
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-unchecked",
      "-language:postfixOps",
      "-Xlint:inaccessible",
      "-Xlint:unsound-match",
      "-Xfatal-warnings",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Ywarn-unused",
      "-Xfuture"
    ),
    scalacOptions in(Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings"),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "rho-swagger" % http4sRhoVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.google.guava" % "guava" % GuavaVersion,
      "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
      "org.scalamock" %% "scalamock" % ScalaMockVersion % Test,
      "org.scalacheck" %% "scalacheck" % ScalaCheckVersion % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")
  ).settings(
  //Build info
  buildInfoObject := "SbtBuildInfo",
  buildInfoPackage := "com.mailinator",
  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, description),
  buildInfoOptions += BuildInfoOption.BuildTime
)
