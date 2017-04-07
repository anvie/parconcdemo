organization := "com.ansvia"

name := "parconcdemo"

version := "0.0.1"

description := "Parallel and Concurrent Programming Demo for Scala.id Meetup April 2017"

scalaVersion := "2.10.4"


resolvers ++= Seq(
	"Sonatype Releases" at "https://oss.sonatype.org/content/groups/scala-tools",
	"typesafe repo"   at "http://repo.typesafe.com/typesafe/releases",
	"Ansvia release repo" at "http://scala.repo.ansvia.com/releases",
	"Ansvia snapshot repo" at "http://scala.repo.ansvia.com/nexus/content/repositories/snapshots"
)

libraryDependencies ++= Seq(
    "org.specs2" %% "specs2" % "1.14",
    "ch.qos.logback" % "logback-classic" % "1.0.13",
    "net.databinder" %% "dispatch-http" % "0.8.10",
    "net.databinder" %% "dispatch-jsoup" % "0.8.10"
)


publishTo <<= version { (v:String) =>
    val ansviaRepo = "http://scala.repo.ansvia.com/nexus"
    if(v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at ansviaRepo + "/content/repositories/snapshots")
    else
        Some("releases" at ansviaRepo + "/content/repositories/releases")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

crossPaths := false

pomExtra := (
  <url>http://ansvia.com</url>
  <developers>
    <developer>
      <id>anvie</id>
      <name>Robin Sy</name>
      <url>http://www.mindtalk.com/u/robin</url>
    </developer>
  </developers>)


