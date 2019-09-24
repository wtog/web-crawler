logLevel := Level.Warn

resolvers += Classpaths.sbtPluginReleases
resolvers += Resolver.sonatypeRepo("snapshots")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.0-RC5")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.1")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("pl.project13.scala" % "sbt-jmh" %  "0.3.4")