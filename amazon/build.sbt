name := "toolkit.amazon"

organization := "net.tqft.toolkit"

version := "0.1.5"

//scalaVersion := "2.9.2"
scalaVersion := "2.10.0-M3"

// in order to compile against 2.10.0-M3, you need to run the hack described at http://www.scala-lang.org/node/12251
crossScalaVersions := Seq("2.9.1", "2.9.2", "2.10.0-M3")

resolvers ++= Seq(
	"Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
	"tqft.net Maven repository" at "http://tqft.net/releases",
	"Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
	"Sonatype Nexus Releases" at "https://oss.sonatype.org/content/repositories/releases",
	"Scala Snapshots" at "http://scala-tools.org/repo-snapshots/"
)

libraryDependencies ++= Seq(
	"junit" % "junit" % "4.8" % "test",
        "net.java.dev.jets3t" % "jets3t" % "0.8.1",
        "com.google.code.typica" % "typica" % "1.7.2"
)

// Scalatest
libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
	val (scalatestVersion, scalatestScalaVersion) = sv match {
		case sv if sv.startsWith("2.9") => ("1.8.RC2", "2.9.0")
		case sv if sv.startsWith("2.10") => ("1.8-SNAPSHOT", "2.10.0-M3")
	}
    	deps :+ ("org.scalatest" % ("scalatest_" + scalatestScalaVersion) % scalatestVersion % "test" )
}

//scalacOptions += "-Xprint:typer"

publishTo := Some(Resolver.sftp("toolkit.tqft.net Maven repository", "tqft.net", "tqft.net/releases") as ("scottmorrison", new java.io.File("/Users/scott/.ssh/id_rsa")))
