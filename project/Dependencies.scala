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
    val seleniumhq = "4.0.0-alpha-2"
    val httpClient = "2.10.1"
    val jackson = "2.9.10"
    val guava = "28.1-jre"
    val typesafeConfig = "1.3.3"
    val scalatest = "3.0.8"
    val hikariCP = "3.4.1"
  }


  implicit class ModuleIDWrapper(moduleID: ModuleID) {
    def provided: ModuleID = moduleID.withConfigurations(Some("provided"))
    def test: ModuleID = moduleID.withConfigurations(Some("test"))
  }

  lazy val crossVersion = Seq("2.12.8", "2.11.11")

  lazy val guava = "com.google.guava" % "guava" % Versions.guava

  lazy val typesafeConfig = "com.typesafe" % "config" % Versions.typesafeConfig

  lazy val jackson = Seq("com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jackson)

  lazy val scalatest = "org.scalatest" %% "scalatest" % Versions.scalatest

  lazy val log = Seq(
    "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0",
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % Versions.log4j2,
    "org.apache.logging.log4j" % "log4j-api" % Versions.log4j2,
    "org.apache.logging.log4j" % "log4j-core" % Versions.log4j2)


  object utils {
    lazy val dependencies = scalatest.test +: (Seq(guava, typesafeConfig) ++ jackson).map(_.provided)
  }

  object core {

    lazy val derby = Seq(
      "com.typesafe.akka" %% "akka-actor" % Versions.akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % Versions.akkaVersion)

    lazy val quartz = "org.quartz-scheduler" % "quartz" % "2.3.1"

    lazy val httpUtils = Seq("org.asynchttpclient" % "async-http-client" % Versions.httpClient)

    lazy val httpParser = Seq("us.codecraft" % "xsoup" % "0.3.1")

    lazy val selenium = Seq(
      "org.seleniumhq.selenium" % "selenium-chrome-driver" % Versions.seleniumhq
    )

    lazy val test = Seq(
      "com.typesafe.akka" %% "akka-testkit" % Versions.akkaVersion,scalatest
       ).map(_.test)

    lazy val dependencies = Seq(quartz, guava, typesafeConfig) ++ jackson ++ derby ++ log ++ httpParser ++ httpUtils ++ test ++ selenium
  }

  object pipeline {
    lazy val postgresql = "42.2.6"

    val pg: ModuleID = "org.postgresql" % "postgresql" % postgresql

    val hikari = "com.zaxxer" % "HikariCP" % Versions.hikariCP

    lazy val h2 = "com.h2database" % "h2" % "1.4.192"
    
    lazy val dependencies = Seq(scalatest, h2).map(_.test) ++ Seq(pg, hikari).map(_.provided)
  }

  object example {
    lazy val dependencies = log ++ Seq(pipeline.pg, pipeline.hikari)
  }

}
