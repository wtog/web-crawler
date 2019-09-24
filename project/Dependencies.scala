import sbt._

/**
 * @author : tong.wang
 * @since : 2018-12-08 00:25
 * @version : 1.0.0
 */
object Dependencies {
  
  object Versions {
    val akkaVersion = "2.5.23"
    val log4j2 = "2.11.0"
    val json4s ="3.6.6"
    val seleniumhq ="4.0.0-alpha-2"
    val httpClient = "2.10.1"
    val jackson = "2.9.10"
  }
  
  import Versions._
  
  lazy val derby = Seq(
    "com.google.guava" % "guava" % "28.0-jre",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion)

  lazy val log = Seq(
    "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0",
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4j2,
    "org.apache.logging.log4j" % "log4j-api" % log4j2,
    "org.apache.logging.log4j" % "log4j-core" % log4j2)

  lazy val quartz = "org.quartz-scheduler" % "quartz" % "2.3.1"

  lazy val httpUtils = Seq("org.asynchttpclient" % "async-http-client" % httpClient)

  lazy val httpParser = Seq("us.codecraft" % "xsoup" % "0.3.1")

  lazy val selenium = Seq(
    "org.seleniumhq.selenium" % "selenium-chrome-driver" % seleniumhq
  )
  
  lazy val json = Seq("com.fasterxml.jackson.module" %% "jackson-module-scala" % jackson)
  
  lazy val test = Seq(
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.0.8" % Test)
  
  val crossVersion = Seq("2.12.8", "2.11.11")

  lazy val dependencies = quartz +: (derby ++ log ++ httpParser ++ httpUtils ++ json ++ test ++ selenium)
}
