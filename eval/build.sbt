libraryDependencies ++= Seq(
	"junit" % "junit" % "4.8" % "test"
)

// Scalatest
libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
	val (scalatestVersion, scalatestScalaVersion) = sv match {
		case sv if sv.startsWith("2.9") => ("1.8.RC2", "2.9.0")
		case sv if sv.startsWith("2.10") => ("1.8-SNAPSHOT", "2.10.0-M3")
	}
    	deps :+ ("org.scalatest" % ("scalatest_" + scalatestScalaVersion) % scalatestVersion % "test" )
}

// the compiler
libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
        deps :+ ("org.scala-lang" % "scala-compiler" % sv)
}

