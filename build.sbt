import Dependencies.dependencies
import Dependencies.crossVersion

lazy val ver = "0.1.2-SNAPSHOT"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

crossScalaVersions := crossVersion

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= dependencies).
  settings(
    assemblyConfig
  )
  .settings(
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions ++= List("-Yrangepos", "-Ywarn-unused-import")
  )

lazy val commonSettings = Seq(
  name := "web-crawler",
  organization := "io.github.wtog",
  version := ver,
  scalaVersion := crossVersion.head,
  fork := true
)

mappings in(Compile, packageBin) ~= {
  _.filter(!_._1.getName.contentEquals("log4j2.xml"))
}

lazy val assemblyConfig = Seq(
  assemblyJarName in assembly := s"web-crawler-assembly.jar",
  mainClass in Compile := Some("io.github.wtog.Main"),
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs@_*) =>
      (xs map {
        _.toLowerCase
      }) match {
        case "maven" :: _ =>
          MergeStrategy.discard
        case "services" :: _ =>
          MergeStrategy.first
        case ("manifest.mf" :: Nil) =>
          MergeStrategy.discard
        case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
          MergeStrategy.filterDistinctLines
        case _ => MergeStrategy.discard
      }
    case _ => MergeStrategy.first
  }
)

javaOptions := Seq(
  "-Dlog4j.configurationFile=log4j2.xml", 
  "-Xmx512m", "-XX:+UnlockCommercialFeatures", "-XX:+FlightRecorder", "-XX:+UnlockDiagnosticVMOptions", "-XX:+DebugNonSafepoints", 
  s"-XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=/tmp/web-crawler-${System.currentTimeMillis()}.jfr"
)