import Dependencies.dependencies
import Dependencies.crossVersion

lazy val ver = "0.1.0-SNAPSHOT"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

crossScalaVersions := crossVersion

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    assemblyConfig,
    libraryDependencies ++= dependencies
  )
  .settings(
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions ++= List("-Yrangepos", "-Ywarn-unused-import")
  )

lazy val commonSettings = Seq(
  name := "web-crawler",
  organization := "io.github.wtog",
  version := ver,
  scalaVersion := crossVersion.head
)

mappings in(Compile, packageBin) ~= {
  _.filter(!_._1.getName.contentEquals("log4j.xml"))
}

lazy val assemblyConfig = Seq(
  assemblyJarName in assembly := s"web-crawler-assembly-${ver}.jar",
  mainClass in Compile := Some("io.github.wtog.Main"),
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs@_*) =>
      (xs map {
        _.toLowerCase
      }) match {
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

Format.formatSettings