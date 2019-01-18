import sbt._
import sbt.Keys._

/**
  * @author : tong.wang
  * @since : 2018-12-20 22:00
  * @version : 1.0.0
  */
object Publish extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".sbt" / ".credentials-center"),

    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2/")
    },

    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true },

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
  )
}
