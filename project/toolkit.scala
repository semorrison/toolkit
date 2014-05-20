import sbt._
import Keys._
import com.typesafe.sbt.SbtProguard._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys

object Toolkit extends Build {
  import BuildSettings._
  import Dependencies._

  lazy val root = Project(id = "toolkit",
    base = file("."),
    settings = buildSettings) aggregate (
        base, 
        arithmetic,
        mathematica, 
        amazon, 
        collections, 
        permutations, 
        orderings, 
        algebra, 
        `algebra-apfloat`,
        `algebra-polynomials`,
        `algebra-categories`,
        `algebra-matrices`,
        `algebra-groups`,
        `algebra-graphs`,
        `algebra-numberfields`,
        `algebra-mathematica`,
        `algebra-spiders`,
        `algebra-experimental`, 
        functions, 
        eval, 
        wiki)

  lazy val base: Project = Project(id = "toolkit-base",
    base = file("base"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(commons.logging))) dependsOn ()

  lazy val arithmetic = Project(id = "toolkit-arithmetic",
    base = file("arithmetic"),
    settings = buildSettings) dependsOn ()

  lazy val collections = Project(id = "toolkit-collections",
    base = file("collections"),
    settings = buildSettings) dependsOn (base, functions)

  lazy val mathematica = Project(id = "toolkit-mathematica",
    base = file("mathematica"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(omath.parser, apfloat))) dependsOn ()

  lazy val amazon = Project(id = "toolkit-amazon",
    base = file("amazon"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(httpclient, jets3t, typica, commons.io))) dependsOn (base, collections)

  lazy val orderings = Project(id = "toolkit-orderings",
    base = file("orderings"),
    settings = buildSettings) dependsOn ()   

  lazy val permutations = Project(id = "toolkit-permutations",
    base = file("permutations"),
    settings = buildSettings) dependsOn (orderings)   

  lazy val algebra = Project(id = "toolkit-algebra",
    base = file("algebra"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq())) dependsOn (arithmetic /* for 'mod' */)

  lazy val `algebra-apfloat` = Project(id = "toolkit-algebra-apfloat",
    base = file("algebra-apfloat"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(apfloat))) dependsOn (algebra)

  lazy val `algebra-polynomials` = Project(id = "toolkit-algebra-polynomials",
    base = file("algebra-polynomials"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq())) dependsOn (orderings, algebra, collections)

  lazy val `algebra-mathematica` = Project(id = "toolkit-algebra-mathematica",
    base = file("algebra-mathematica"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq())) dependsOn (mathematica, `algebra-polynomials`)

  lazy val `algebra-categories` = Project(id = "toolkit-algebra-categories",
    base = file("algebra-categories"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq())) dependsOn (algebra, `algebra-polynomials`)

  lazy val `algebra-matrices` = Project(id = "toolkit-algebra-matrices",
    base = file("algebra-matrices"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq())) dependsOn (collections, permutations, algebra, `algebra-polynomials`, `algebra-categories`)

  lazy val `algebra-numberfields` = Project(id = "toolkit-algebra-numberfields",
    base = file("algebra-numberfields"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq())) dependsOn (`algebra-polynomials`, `algebra-matrices`)

  lazy val `algebra-groups` = Project(id = "toolkit-algebra-groups",
    base = file("algebra-groups"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq())) dependsOn (functions, permutations, algebra, `algebra-polynomials`, `algebra-matrices`, `algebra-numberfields`)

  lazy val `algebra-graphs` = Project(id = "toolkit-algebra-graphs",
    base = file("algebra-graphs"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq())) dependsOn (base, `algebra-groups`)

  lazy val `algebra-spiders` = Project(id = "toolkit-algebra-spiders",
    base = file("algebra-spiders"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(mapdb))) dependsOn (algebra, mathematica, `algebra-mathematica`, `algebra-polynomials`, `algebra-graphs`, `algebra-matrices`, `algebra-numberfields`, `algebra-apfloat`)

  lazy val `algebra-bugs` = Project(id = "toolkit-algebra-bugs",
    base = file("algebra-bugs"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq())) dependsOn (mathematica, `algebra-mathematica`)

  lazy val `algebra-experimental` = Project(id = "toolkit-algebra-experimental",
    base = file("algebra-experimental"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(commons.math, apfloat, guava, findbugs))) dependsOn (amazon, functions, collections, algebra, `algebra-categories`, `algebra-polynomials`, `algebra-groups`, `algebra-graphs`, `algebra-matrices`, `algebra-numberfields`, `algebra-apfloat`)

  lazy val functions = Project(id = "toolkit-functions",
    base = file("functions"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(guava, findbugs)))

  lazy val eval = Project(id = "toolkit-eval",
    base = file("eval"),
    settings = buildSettings ++ Seq(dependsOnCompiler))

  lazy val wiki = Project(id = "toolkit-wiki",
    base = file("wiki"),
    settings = buildSettings ++ 
      proguardSettings ++ 
      Seq(
        javaOptions in (Proguard, ProguardKeys.proguard) := Seq("-Xmx2G"), 
        ProguardKeys.options in Proguard ++= Seq("-dontnote", "-dontwarn", "-ignorewarnings"),
        ProguardKeys.options in Proguard += ProguardOptions.keepMain("net.tqft.toolkit.wiki.Wiki$")
      ) ++ 
      OneJar.settings ++ 
      Seq(libraryDependencies ++= Seq(selenium.firefox, selenium.htmlunit, mysql, slick))) dependsOn (base)

}

object BuildSettings {
  import Resolvers._
  import Dependencies._

  val buildOrganization = "net.tqft"
  val buildVersion = "0.1.18-SNAPSHOT"
 // val buildScalaVersion = "2.10.4"
  val buildScalaVersion = "2.11.0"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    publishTo := Some(Resolver.sftp("toolkit.tqft.net Maven repository", "tqft.net", "tqft.net/releases") as ("scottmorrison", new java.io.File("/Users/scott/.ssh/id_rsa"))),
    resolvers := sonatypeResolvers ++ tqftResolvers /* ++ SonatypeSettings.publishing */,
    libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.4" % "test",
    //    scalacOptions ++= Seq("-uniqid","-explaintypes"),
//    scalacOptions ++= Seq("-optimise" /*,"-Yinline-warnings"*/),
    libraryDependencies ++= Seq(junit, slf4j),
    exportJars := true,
    EclipseKeys.withSource := true
  )

  val dependsOnCompiler = libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) => deps :+ ("org.scala-lang" % "scala-compiler" % sv) }
}

object OneJar {
    import com.github.retronym.SbtOneJar._
    val settings = oneJarSettings ++ Seq(exportJars := true, mainClass in oneJar := Some("org.omath.ui.repl.omath"))
}


object SonatypeSettings {
/*
  val publishing = Seq(
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { x => false },
    pomExtra := (
      <url>http://bitbucket.org/scottmorrison/toolkit</url>
      <licenses>
        <license>
          <name>CC-BY</name>
          <url>http://creativecommons.org/licenses/by/3.0/</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>https://bitbucket.org/scottmorrison/toolkit</url>
      </scm>
      <developers>
        <developer>
          <id>scott</id>
          <name>Scott Morrison</name>
          <url>http://tqft.net</url>
        </developer>
      </developers>),
    publishTo <<= version { v: String =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    })
    */
}

object Resolvers {
  val sonatypeResolvers = Seq(
    "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype Nexus Releases" at "https://oss.sonatype.org/content/repositories/releases")
  val tqftResolvers = Seq(
	"tqft.net Maven repository" at "http://tqft.net/releases"	
  )
}

object Dependencies {
	val junit = "junit" % "junit" % "4.11" % "test"
	val slf4j = "org.slf4j" % "slf4j-log4j12" % "1.6.1"
	val apfloat = "org.apfloat" % "apfloat" % "1.6.3"		// arbitrary precision integers and floats; much better than BigInt and BigDecimal
	object commons {
		val math = "org.apache.commons" % "commons-math" % "2.2"	// simplex algorithm
		val logging = "commons-logging" % "commons-logging" % "1.1.1"
		val io = "commons-io" % "commons-io" % "2.4"
	}
	object scala {
		val xml = "org.scala-lang.modules" %% "scala-xml" % "1.0.1"
	}
	val httpclient = "org.apache.httpcomponents" % "httpclient" % "4.3.2"
	val jets3t = "net.java.dev.jets3t" % "jets3t" % "0.9.0"
	val typica = "com.google.code.typica" % "typica" % "1.7.2"
	val guava = "com.google.guava" % "guava" % "16.0.1"
	val findbugs = "com.google.code.findbugs" % "jsr305" % "1.3.9"
	object selenium {
		val firefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % "2.40.0"
		val htmlunit = "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.40.0"
	}
	object lift {
		val util = "net.liftweb" %% "lift-util" % "2.6-M2"
	}
	val mysql = "mysql" % "mysql-connector-java" % "5.1.24"
	val mapdb = "org.mapdb" % "mapdb" % "0.9.9"
	val slick = "com.typesafe.slick" % "slick_2.11.0-RC4" % "2.1.0-M1"
	val scalaz = "org.scalaz" %% "scalaz-core" % "7.1.0-M6"
	val spire = "org.spire-math" %% "spire" % "0.7.1"
	object omath {
		val parser = "org.omath" %% "omath-parser" % "0.0.1-SNAPSHOT"
	}
}

