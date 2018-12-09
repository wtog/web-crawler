import Dependencies.dependencies
import Dependencies.crossVersion

lazy val ver = "0.1.0-SNAPSHOT"

javacOptions++=Seq("-source","1.8", "-target","1.8")

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

mappings in (Compile, packageBin) ~= { _.filter(!_._1.getName.contentEquals("log4j.xml")) }

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2/")
}

publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := { _ => true }

pomExtra in Global := {
  <url>https://github.com/wtog/web-crawler</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:wtog/web-crawler.git</url>
      <connection>scm:git:git@github.com:wtog/web-crawler.git</connection>
    </scm>
    <developers>
      <developer>
        <id>wangtong</id>
        <name>wangtong</name>
        <url>https://github.com/wtog/</url>
      </developer>
    </developers>
}

lazy val assemblyConfig = Seq(
  assemblyJarName in assembly := s"web-crawler-assembly-${ver}.jar",
  mainClass in Compile := Some("io.github.wtog.Main"),
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

Format.formatSettings