import xml.Group
// import scalariform.formatter.preferences._
organization := "io.backchat.hookup"

name := "hookup"

version := "0.4.3-SNAPSHOT"

scalaVersion := "2.12.7"

compileOrder := CompileOrder.ScalaThenJava

libraryDependencies ++= Seq(
  "io.netty" % "netty" % "3.10.4.Final",
  "com.github.nscala-time" %% "nscala-time" % "2.14.0",
  "org.json4s" %% "json4s-jackson" % "3.2.11" % "compile",
  "commons-io" % "commons-io" % "2.4",
  "com.typesafe.akka" %% "akka-actor" % "2.4.12" % "compile",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.12" % "test",
  "org.specs2" %% "specs2-core" % "3.8.6" % "test",
  "org.specs2" %% "specs2-junit" % "3.8.6" % "test",
  "junit" % "junit" % "4.11" % "test",
  "joda-time" % "joda-time" % "2.8.2"
)

scalacOptions ++= Seq(
  "-optimize",
  "-deprecation",
  "-unchecked",
  "-Xcheckinit",
  "-Yrangepos",
  "-encoding", "utf8")

parallelExecution in Test := false

testOptions := Seq(Tests.Argument("console", "junitxml"))

scalacOptions ++= Seq("-language:implicitConversions")

lazy val root = (project in file(".")).
enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "io.backchat.hookup"
  )

