package net.tqft.toolkit.algebra.graphs

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._

@RunWith(classOf[JUnitRunner])
class GraphTest extends FlatSpec with Matchers {

  "relabel" should "work" in {
    val g = Graph(5, IndexedSeq(Seq(1), Seq(0), Seq(3), Seq(4), Seq(2)))
   g.relabel(IndexedSeq(0,1,2,4,3)) should equal(g)
  }
  
  
}