import Dependencies.crossVersion
import sbt.Keys.organization
import sbtassembly.{Assembly, MergeStrategy, PathList}

lazy val ver = "0.1.2-SNAPSHOT"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

lazy val scalafixSettings = Seq(
  addCompilerPlugin(scalafixSemanticdb),
  scalacOptions ++= List("-Yrangepos", "-Ywarn-unused-import"))

lazy val jmhSettings = Seq(
  sourceDirectory in Jmh := (sourceDirectory in Test).value,
  classDirectory in Jmh := (classDirectory in Test).value,
  dependencyClasspath in Jmh := (dependencyClasspath in Test).value
)

lazy val commonSettings = Seq(
  version := ver,
  scalaVersion := crossVersion.head,
  fork := true,
  crossScalaVersions := crossVersion,
  parallelExecution in Test := true
)

lazy val utils = (project in file("utils"))
  .settings(commonSettings: _*)
  .settings(scalafixSettings: _*)
  .settings(jmhSettings: _*)
  .settings(Seq(name := "utils", organization := "io.github.wtog.utils"))
  .settings(libraryDependencies ++= Dependencies.utils.dependencies)
  .disablePlugins(AssemblyPlugin)

lazy val core = (project in file("crawler-core"))
  .settings(commonSettings: _*)
  .settings(scalafixSettings: _*)
  .settings(jmhSettings: _*)
  .settings(Seq(name := "crawler-core", organization := "io.github.wtog.crawler"))
  .settings(libraryDependencies ++= Dependencies.core.dependencies)
  .dependsOn(utils)
  .disablePlugins(AssemblyPlugin)

lazy val pipeline = (project in file("crawler-pipeline"))
  .settings(commonSettings: _*)
  .settings(scalafixSettings: _*)
  .settings(jmhSettings: _*)
  .settings(Seq(name := "crawler-pipeline", organization := "io.github.wtog.crawler.pipeline"))
  .settings(libraryDependencies ++= Dependencies.pipeline.dependencies)
  .dependsOn(core)
  .disablePlugins(AssemblyPlugin)

lazy val example = (project in file("crawler-example"))
  .settings(commonSettings: _*)
  .settings(scalafixSettings: _*)
  .settings(Seq(name := "crawler-example", organization := "io.github.wtog.example"))
  .settings(libraryDependencies ++= Dependencies.example.dependencies)
  .settings(
    Seq(
      assemblyJarName in assembly := s"web-crawler-assembly.jar",
      mainClass in Compile := Some("io.github.wtog.example.Main"),
      test in assembly := {},
      assemblyMergeStrategy in assembly := {
        case x if Assembly.isConfigFile(x) =>
          MergeStrategy.concat
        case PathList(ps@_*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
          MergeStrategy.rename
        case PathList(ps@_*) if Assembly.isSystemJunkFile(ps.last) =>
          MergeStrategy.discard
        case PathList("META-INF", xs@_*) =>
          xs.map(_.toLowerCase) match {
            case (x :: Nil) if Seq("manifest.mf", "index.list", "dependencies") contains x =>
              MergeStrategy.discard
            case ps@(x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") || ps.last.endsWith(".rsa") =>
              MergeStrategy.discard
            case "maven" :: xs =>
              MergeStrategy.discard
            case "plexus" :: xs =>
              MergeStrategy.discard
            case "services" :: xs =>
              MergeStrategy.filterDistinctLines
            case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) | ("spring.tooling" :: Nil) =>
              MergeStrategy.filterDistinctLines
            case _ => MergeStrategy.first
          }
        case _ => MergeStrategy.first
      }
    )
  )
  .dependsOn(core, pipeline)
  .enablePlugins(DisablePublish, AssemblyPlugin)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(Seq(name := "web-crawler"))
  .aggregate(utils, core, pipeline, example)
  .enablePlugins(JmhPlugin, DisablePublish)
  .disablePlugins(AssemblyPlugin)

testOptions in Test += Tests.Argument(s"-P${java.lang.Runtime.getRuntime.availableProcessors()}")

javaOptions in test := Seq(
  "-Dlog4j.configurationFile=log4j2-test.xml",
  "-Xms512m", "-Xmx512m"
)

javaOptions in run := Seq(
  "-Dlog4j.configurationFile=log4j2.xml",
  "-Xms512m", "-Xmx512m"
)
