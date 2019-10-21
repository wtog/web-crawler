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
    val seleniumhq ="4.0.0-alpha-2"
    val httpClient = "2.10.1"
    val jackson = "2.9.10"
    val guava = "28.0-jre"
    val typesafeConfig = "1.3.3"
  }
  
  import Versions._

  implicit class ModuleIDWrapper(moduleID: ModuleID){
    def provided: ModuleID = moduleID.withConfigurations(Some("provided"))
  }

  lazy val crossVersion = Seq("2.12.8", "2.11.11")

  lazy val guava = "com.google.guava" % "guava" % Versions.guava

  lazy val typesafeConfig = "com.typesafe" % "config" % Versions.typesafeConfig

  lazy val jackson = Seq("com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jackson)

  lazy val log = Seq(
    "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0",
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4j2,
    "org.apache.logging.log4j" % "log4j-api" % log4j2,
    "org.apache.logging.log4j" % "log4j-core" % log4j2)

  
  object utils {
    lazy val guavaProvided = guava.provided
    lazy val typesafeConfigProvided = typesafeConfig.provided
    lazy val jacksonProvided = jackson.map(_.provided)
    lazy val dependencies  = Seq(guavaProvided, typesafeConfigProvided) ++ jacksonProvided
  }

  object core {

    lazy val derby = Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion)

    lazy val quartz = "org.quartz-scheduler" % "quartz" % "2.3.1"

    lazy val httpUtils = Seq("org.asynchttpclient" % "async-http-client" % httpClient)

    lazy val httpParser = Seq("us.codecraft" % "xsoup" % "0.3.1")

    lazy val selenium = Seq(
      "org.seleniumhq.selenium" % "selenium-chrome-driver" % seleniumhq
    )

    lazy val test = Seq(
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.8" % Test)

    lazy val dependencies = Seq(quartz, guava, typesafeConfig) ++ jackson ++ derby ++ log ++ httpParser ++ httpUtils ++ test ++ selenium
  }

  object pipeline {
    lazy val dependencies = Seq()
  }

  object example {
    lazy val dependencies = log
  }

}
