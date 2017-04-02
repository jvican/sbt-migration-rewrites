lazy val `sbt-api-migrator` = project.in(file("."))

name := "sbt-api-migrator"

organization := "me.vican.jorge"

// Using 2.11.x until scalafix publishes 2.12 artifacts
scalaVersion := crossScalaVersions.value.head

crossScalaVersions := Seq("2.11.8", "2.12.1")

resolvers += Resolver.bintrayRepo("jvican", "releases")

libraryDependencies ++= Vector(
  "ch.epfl.scala" %% "scalafix-cli" % "0.3.3-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

licenses := Seq("MPL-2.0" -> url("https://opensource.org/licenses/MPL-2.0"))
pomExtra in Global := {
  <developers>
    <developer>
      <id>jvican</id>
      <name>Jorge Vicente Cantero</name>
      <url>https://github.com/jvican</url>
    </developer>
  </developers>
}
