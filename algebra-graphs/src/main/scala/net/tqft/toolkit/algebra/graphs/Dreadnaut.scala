package net.tqft.toolkit.algebra.graphs

import scala.sys.process._
import net.tqft.toolkit.Logging
import java.io.InputStream
import java.io.OutputStream
import scala.io.Source
import java.io.PrintWriter
import java.io.File
import net.tqft.toolkit.algebra.grouptheory.FinitelyGeneratedFiniteGroup
import net.tqft.toolkit.algebra.grouptheory.FiniteGroups

trait Dreadnaut extends Logging {
  def dreadnautPath: String

  private case class Pipes(var in: PrintWriter, var out: Iterator[String], var err: Iterator[String])

  private val pipes = new ThreadLocal[Pipes]

  private def in = pipes.get.in
  private def out = pipes.get.out
  private def err = pipes.get.err

  protected def initializeDreadnaut = {
    if (pipes.get == null || in == null) {
      val p = Pipes(null, null, null)
      pipes.set(p)
      dreadnautPath.run(new ProcessIO(os => p.in = new PrintWriter(os), is => p.out = Source.fromInputStream(is).getLines, is => p.err = Source.fromInputStream(is).getLines))
      while (in == null || out == null) {
        Thread.sleep(10)
      }
      p.out = out.filterNot(line => line.startsWith("Mode=") || line.startsWith("linelen="))
    }
  }

  def invokeDreadnaut(cmd: String): Seq[String] = {
    initializeDreadnaut

    in.println(cmd)
    in.println("\"done... \"z")
    for (i <- 0 until 137) in.println("?") // hideous hack, because somewhere along the way dreadnaut's output is being buffered
    in.flush()
    val result = out.takeWhile(!_.startsWith("done... [")).toList
    result
  }

  def automorphismGroupAndOrbits(g: Graph): (FinitelyGeneratedFiniteGroup[IndexedSeq[Int]], Seq[Seq[Int]]) = {
    //    println("invoking dreadnaut: ")
    //    println(g.toDreadnautString + "cxo\n")
    val output = invokeDreadnaut(g.toDreadnautString + "cxo\n")
    //        println("output: ")
    //        println(output.mkString("\n"))
    val generatorsString = {
      import net.tqft.toolkit.collections.Split._
      output.takeWhile(l => !l.startsWith("canupdates")).filter(line => line.trim.startsWith("(") || line.startsWith(" ")).iterator.splitBefore(_.startsWith("(")).filter(_.nonEmpty).map(_.map(_.trim).mkString(" ")).toStream
    }
    //    println(s"generatorsString: \n${generatorsString.mkString("\n")}")
    def permutationFromCycles(cycles: Array[Array[Int]]): IndexedSeq[Int] = {
      for (i <- 0 until g.numberOfVertices) yield {
        cycles.find(_.contains(i)) match {
          case Some(cycle) => cycle((cycle.indexOf(i) + 1) % cycle.length)
          case None => i
        }
      }
    }
    val generators = generatorsString.map(line => permutationFromCycles(line.trim.split('(').filter(_.nonEmpty).map(_.trim.stripSuffix(")").split(" ").map(_.toInt)))).toSet
    for (x <- generators) {
      require(g.relabel(x) == g.relabel(IndexedSeq.range(0, g.numberOfVertices)),
        "something went wrong while calling dreadnaut '" + g.toDreadnautString + "cxo':\n" +
          "generators:\n" + generators.mkString("\n") + "\n" +
          "output:\n" + output.mkString("\n") +
          s"looking at generator x = \n$x\n${x.zipWithIndex.map(_.swap)}\n we have:\n" +
          s"g.relabel(x) = \n${g.relabel(x)} = \n${g.relabel(x).toDreadnautString} != \n${g.relabel(IndexedSeq.range(0, g.numberOfVertices))} = \n${g.relabel(IndexedSeq.range(0, g.numberOfVertices)).toDreadnautString} = \ng.relabel(IndexedSeq.range(0, g.numberOfVertices)")
    }
    val automorphismGroup = FiniteGroups.symmetricGroup(g.numberOfVertices).subgroupGeneratedBy(generators)
    val orbits = output.dropWhile(l => !l.startsWith("canupdates")).tail.mkString("").split(";").toSeq.map(_.trim).filter(_.nonEmpty).map({ orbitString =>
      orbitString.split(" ").toSeq.map(_.trim).filter(_.nonEmpty).filter(!_.startsWith("(")).flatMap({ s =>
        if (s.contains(":")) {
          val a = s.split(":").map(_.toInt)
          a(0) to a(1)
        } else {
          Seq(s.toInt)
        }
      })
    })
    require(orbits.flatten.sorted == (0 until g.numberOfVertices), "something went wrong while calling dreadnaut '" + g.toDreadnautString + s"cxo':\norbits: $orbits\noutput:\n" + output.mkString("\n"))
    (automorphismGroup, orbits)
  }
  def automorphismGroup(g: Graph) = automorphismGroupAndOrbits(g)._1

  def findIsomorphism(g1: Graph, g2: Graph): Option[IndexedSeq[Int]] = {
    val output = invokeDreadnaut(g1.toDreadnautString + "c x @\n" + g2.toDreadnautString + "x ##")
    //    for (line <- output) println(line)
    val relevantOutput = output.dropWhile(l => !l.startsWith("canupdates")).tail.dropWhile(l => !l.startsWith("canupdates")).tail
    if (relevantOutput.head == "h and h' are identical.") {
      Some(
        relevantOutput.tail.mkString(" ").split(" ").toIndexedSeq.filter(_.trim.nonEmpty).map(p => p.split("-")(1).toInt))
    } else {
      None
    }
  }

  def canonicalLabelling(g: Graph): IndexedSeq[Int] = {
    val output = invokeDreadnaut(g.toDreadnautString + "cxb\n")
    val result = output.dropWhile(!_.startsWith("canupdates")).tail.takeWhile(!_.startsWith("  0 :")).mkString("").split(' ').filter(_.nonEmpty).map(_.toInt)
    require(result.length == g.numberOfVertices)
    result
  }
  def canonicalize(g: Graph): Graph = {
    g.relabel(canonicalLabelling(g))
  }
  def canonicalizeColouredGraph[W](g: ColouredGraph[W]): ColouredGraph[W] = {
    g.relabel(canonicalLabelling(g))
  }
}

object Dreadnaut extends Dreadnaut {
  override val dreadnautPath = {
    val localDreadnaut = new File("./dreadnaut")
    if (localDreadnaut.exists) {
      localDreadnaut.toPath.toString
    } else {
      try {
        "which dreadnaut".!!
      } catch {
        case e: Exception => System.getProperty("user.home") + "/bin/dreadnaut"
      }
    }
  }

  require(dreadnautPath.nonEmpty, "There doesn't appear to be a copy of dreadnaut on the $PATH.")
  require(invokeDreadnaut("n=3 g 1;2;0; cx").head.startsWith("(1 2)"), "The copy of dreadnaut at " + dreadnautPath + " doesn't seem to be working.")

  initializeDreadnaut
}