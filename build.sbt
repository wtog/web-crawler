lazy val akkaVersion = "2.5.12"

lazy val commonSettings = Seq(
  name := "web-crawler",
  organization := "wt",
  version := "0.1.0",
  scalaVersion := "2.12.4"
)

lazy val derby = Seq(
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.typesafe.akka" % "akka-actor_2.12" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.0",
  
)

lazy val log = Seq(
  "org.slf4j" % "slf4j-log4j12" % "1.7.25"
)

lazy val httpUtils = Seq(
  "org.asynchttpclient" % "async-http-client" % "2.4.7"
)

lazy val httpParser = Seq(
  "org.jsoup" % "jsoup" % "1.10.3",
  "us.codecraft" % "xsoup" % "0.3.1"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= derby ++ log ++ httpUtils ++ httpParser
  )