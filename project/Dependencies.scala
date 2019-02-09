import sbt._

/**
 * @author : tong.wang
 * @since : 2018-12-08 00:25
 * @version : 1.0.0
 */
object Dependencies {
  lazy val akkaVersion = "2.5.12"
  lazy val derby = Seq(
    "org.mindrot" % "jbcrypt" % "0.3m",
    "com.google.guava" % "guava" % "23.5-jre",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "org.scalatest" %% "scalatest" % "3.0.1" % "test")

  val log4j2 = "2.11.0"
  lazy val log = Seq(
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4j2,
    "org.apache.logging.log4j" % "log4j-api" % log4j2,
    "org.apache.logging.log4j" % "log4j-core" % log4j2)

  lazy val quartz = "org.quartz-scheduler" % "quartz" % "2.3.0"

  lazy val httpUtils = Seq(
    "org.asynchttpclient" % "async-http-client" % "2.5.3" excludeAll (ExclusionRule("org.reactivestreams", "reactive-streams"), ExclusionRule("io.netty", "netty-handler")),
    "org.apache.httpcomponents" % "httpclient" % "4.5.2")

  lazy val httpParser = Seq(
    "us.codecraft" % "xsoup" % "0.3.1")

  lazy val json = Seq(
    "org.json4s" %% "json4s-native" % "3.6.1",
    "org.json4s" %% "json4s-jackson" % "3.6.1")

  lazy val dependencies = derby ++ log ++ httpParser ++ httpUtils ++ json :+ quartz
  val crossVersion = Seq("2.12.8", "2.11.12")
}
