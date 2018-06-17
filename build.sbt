lazy val ver = "0.1.0"

javacOptions++=Seq("-source","1.8","-target","1.8")

lazy val commonSettings = Seq(
  name := "web-crawler",
  organization := "wt",
  version := ver,
  scalaVersion := "2.12.4"
)

lazy val akkaVersion = "2.5.12"
lazy val derby = Seq(
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.typesafe.akka" % "akka-actor_2.12" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.0"
)

lazy val log = Seq(
  "org.slf4j" % "slf4j-log4j12" % "1.7.25"
)

lazy val httpUtils = Seq(
  "org.asynchttpclient" % "async-http-client" % "2.4.7",
  "org.apache.httpcomponents" % "httpclient" % "4.5.2"
)

lazy val httpParser = Seq(
  "org.jsoup" % "jsoup" % "1.10.3",
  "us.codecraft" % "xsoup" % "0.3.1"
)

lazy val assemblyConfig = Seq(
  assemblyJarName in assembly := s"web-crawler-${ver}.jar",
  mainClass in Compile := Some("wt.Main"),
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) =>
      (xs map {_.toLowerCase}) match {
        case "maven" :: xs =>
          MergeStrategy.discard
        case "services" :: xs =>
          MergeStrategy.first
        case ("manifest.mf" :: Nil) =>
          MergeStrategy.discard
        case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
          MergeStrategy.filterDistinctLines
        case _ => MergeStrategy.discard
      }
    case x => MergeStrategy.first
  }
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    assemblyConfig,
    libraryDependencies ++= derby ++ log ++ httpUtils ++ httpParser
  )
  