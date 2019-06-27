import sbt._

/**
 * @author : tong.wang
 * @since : 2018-12-08 00:25
 * @version : 1.0.0
 */
object Dependencies {
  
  object Versions {
    lazy val akkaVersion = "2.5.23"
    lazy val log4j2 = "2.11.0" 
    lazy val json4s ="3.6.6"
  }
  
  import Versions._
  
  lazy val derby = Seq(
    "com.google.guava" % "guava" % "27.0.1-jre",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion)

  lazy val log = Seq(
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4j2,
    "org.apache.logging.log4j" % "log4j-api" % log4j2,
    "org.apache.logging.log4j" % "log4j-core" % log4j2)

  lazy val quartz = "org.quartz-scheduler" % "quartz" % "2.3.1"

  lazy val httpUtils = Seq("org.asynchttpclient" % "async-http-client" % "2.8.1")

  lazy val httpParser = Seq("us.codecraft" % "xsoup" % "0.3.1")

  lazy val json = Seq(
    "org.json4s" %% "json4s-native" % json4s,
    "org.json4s" %% "json4s-jackson" % json4s)
  
  lazy val test = Seq(
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.0.8" % Test)
  
  val crossVersion = Seq("2.12.8", "2.11.12")

  lazy val dependencies = derby ++ log ++ httpParser ++ httpUtils ++ json ++ test :+ quartz
}
