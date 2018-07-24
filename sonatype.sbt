organization := "io.github.wtog"

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra in Global := {
  <url>https://github.com/TopSpoofer/jslog</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:wang-tong/web-crawler.git</url>
      <connection>scm:git:git@github.com:wang-tong/web-crawler.git</connection>
    </scm>
    <developers>
      <developer>
        <id>wangtong</id>
        <name>wangtong</name>
        <url>https://github.com/wangtong/</url>
      </developer>
    </developers>
}