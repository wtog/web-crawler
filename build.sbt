lazy val ver = "0.1.0"

javacOptions++=Seq("-source","1.8","-target","1.8")

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    assemblyConfig,
    libraryDependencies ++= derby ++ log ++ httpUtils ++ httpParser ++ spark
  )

lazy val commonSettings = Seq(
  name := "web-crawler",
  organization := "io.wt",
  version := ver,
  scalaVersion := "2.11.4"
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2/")
}

lazy val akkaVersion = "2.5.12"
lazy val derby = Seq(
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.google.guava" % "guava" % "23.5-jre",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
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

lazy val spark_version = "2.3.1"
lazy val spark_dep_scope = "test"

lazy val spark = Seq(
  "org.apache.spark" %% "spark-core" % spark_version % spark_dep_scope,
  "org.apache.spark" %% "spark-sql" % spark_version % spark_dep_scope,
  "org.apache.spark" %% "spark-mllib" % spark_version % spark_dep_scope,
  "org.apache.spark" %% "spark-streaming" % spark_version % spark_dep_scope
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
