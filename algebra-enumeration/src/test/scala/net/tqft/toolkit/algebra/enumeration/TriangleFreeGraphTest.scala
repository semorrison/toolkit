package net.tqft.toolkit.algebra.enumeration

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import net.tqft.toolkit.algebra.graphs.Graph
import net.tqft.toolkit.functions.Memo

@RunWith(classOf[JUnitRunner])
class TriangleFreeGraphTest extends FlatSpec with Matchers {

  //  "upperObjects" should "correctly partition into orbits" in {
  //    TriangleFreeGraph(6, Vector(List(1, 5), List(0, 4), List(3), List(2), List(1, 5), List(0, 4))).verifyUpperOrbits.forall(_ == true) should be(true)
  //  }
  //
  //  "children" should "be pairwaise non-isomorphic" in {
  //    def isomorphic_?(a: TriangleFreeGraph, b: TriangleFreeGraph) = {
  //      import net.tqft.toolkit.permutations.Permutations
  //      a.numberOfVertices == b.numberOfVertices && {
  //        Permutations.of(a.numberOfVertices).exists(p => a.relabel(p) == b)
  //      }
  //    }
  //
  //    val g = TriangleFreeGraph(6, Vector(List(1, 5), List(0, 4), List(3), List(2), List(1, 5), List(0, 4)))
  //    for (s <- g.children.toSet.subsets(2)) {
  //      val a :: b :: Nil = s.toList
  //      isomorphic_?(a, b) should be(false)
  //    }
  //  }
  //
  //  "descendants" should "enumerate all triangle free graphs with up to 8 vertices" in {
  //    val n = 8
  //
  //    val g0 = TriangleFreeGraph(1, IndexedSeq(Seq()))
  //    val graphs = g0.descendants(n - _.numberOfVertices).toStream
  //    //    for (g <- graphs) println(g)
  //    graphs.groupBy(_.numberOfVertices).mapValues(_.size).toSeq.sortBy(_._1).map(_._2) should equal(List(1, 2, 3, 7, 14, 38, 107, 410, 1897, 12172, 105071, 1262180, 20797002, 467871369).take(n))
  //  }

  "descendants" should "partition results according to a res/mod pair" in {
    val n = 8

    val g0 = TriangleFreeGraph(1, IndexedSeq(Seq()))

    def resMod_(res: Int, mod: Int) = {
      println(s"computing residue class $res/$mod")
      g0.descendants(n - _.numberOfVertices, res, mod).toSet
    }
    val resMod = Memo(resMod_ _)
    
    def mod(mod: Int) = (for (res <- 0 until mod; x <- resMod(res, mod)) yield x).toSet

    def pairwiseIntersections(mod: Int) = {
      for (i <- 0 until mod; j <- i + 1 until mod) yield resMod(i, mod).intersect(resMod(j, mod))
    }

    pairwiseIntersections(4).foreach(_ should be('empty))
    pairwiseIntersections(15).foreach(_ should be('empty))

    val i = 4
    val j = 6
    for (z <- 0 until j) {
      (for (k <- z until i * j by j; g <- resMod(k, i * j)) yield g).toSet should equal(resMod(z, j))
    }

    mod(1) should equal(mod(3))
    mod(1) should equal(mod(48))
    mod(2) should equal(mod(4))
    mod(72) should equal(mod(36))

    //    println(graphs.size)
    //    for (k <- 0 until 72) println(resMod(k, 72).size)
  }
}