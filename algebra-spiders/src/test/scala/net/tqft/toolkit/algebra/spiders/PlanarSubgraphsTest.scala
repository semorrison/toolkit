package net.tqft.toolkit.algebra.spiders

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._

@RunWith(classOf[JUnitRunner])
class PlanarSubgraphTest extends FlatSpec with Matchers with IsomorphismMatchers {
  val spider = implicitly[Spider[PlanarGraph]]

  import PlanarGraph._

  "a k-gon" should "contain 3k trivalent vertices" in {
    for (k <- 1 to 7) {
      val q = polygon(k)
      q.Subgraphs(star(3)).excisions.size should equal(3 * k)
    }
  }
  "a monogon" should "contain no Is" in {
    val q = polygon(1)
    q.Subgraphs(I).excisions.size should equal(0)
  }
  "a k-gon with k >= 2" should "contain 2k Is" in {
    for (k <- 2 to 7) {
      val q = polygon(k)
      q.Subgraphs(I).excisions.size should equal(2 * k)
    }
  }
  "a k-gon with k >= 2" should "contain 2k Hs" in {
    for (k <- 2 to 7) {
      val q = polygon(k)
      q.Subgraphs(H).excisions.size should equal(2 * k)
    }
  }
  "a k-gon" should "map to itself k times" in {
    for (k <- 1 to 7) {
      val q = polygon(k)
      q.Subgraphs(q).excisions.size should equal(k)
    }
  }
  "some diagrams with monogons" should "contain monogons" in {
    val d1 = PlanarGraph(11, IndexedSeq(List((3, 11), (6, 10), (5, 10), (4, 8)), List((6, 10), (7, 10), (7, 9)), List((3, 10), (4, 11), (5, 8))), IndexedSeq((1,0), (1,0)), 0)
    d1.Subgraphs(polygon(1)).excisions.size should equal(1)
    val d2 = PlanarGraph(8, IndexedSeq(List((3, 8), (5, 10), (4, 9), (3, 10)), List((4, 10), (7, 9), (5, 9)), List((6, 9), (6, 11), (7, 9))), IndexedSeq((1,0), (1,0)), 0)
    d2.Subgraphs(polygon(1)).excisions.size should equal(1)
  }
  "a theta" should "contain ..." in {
    theta.Subgraphs(star(3)).excisions.size should equal(6)
    theta.Subgraphs(I).excisions.size should equal(6)
    
    println(theta.internalFaceSizes)
    println(polygon(2).internalFaceSizes)
    
    theta.Subgraphs(polygon(2)).excisions.size should equal(4)
  }
  "a tetrahedron" should "contain ..." in {
    tetrahedron.Subgraphs(star(3)).excisions.size should equal(12)
    tetrahedron.Subgraphs(I).excisions.size should equal(12)
    tetrahedron.Subgraphs(polygon(3)).excisions.size should equal(9)
  }

  "a dodecahedron" should "contain 55 pentagons" in {
    dodecahedron.Subgraphs(polygon(5)).excisions.size should equal(55)
  }

  "a theta inside another theta" should "only contain 6 bigons" in {
    val q = PlanarGraph(9, IndexedSeq(List(), List((4, 13), (8, 10), (6, 9)), List((3, 13), (7, 11), (5, 12)), List((5, 13), (7, 12), (3, 11)), List((6, 13), (8, 9), (4, 10))), IndexedSeq((1,0),(1,0), (1,0), (1,0)), 0)
    q.Subgraphs(polygon(2)).excisions.size should equal(6)
  }

  "this graph" should "not blow up while finding subgraphs" in {
    val q = PlanarGraph(12, IndexedSeq(List(), List((8, 13), (8, 12), (11, 13)), List((7, 15), (11, 13), (9, 13)), List((4, 16), (5, 13), (6, 17)), List((4, 13), (23, 16), (22, 13)), List((7, 13), (9, 15), (22, 13)), List((5, 17), (23, 13), (6, 16))), IndexedSeq((1,0), (1,0), (1,0), (1,0), (1,0), (1,0)), 0)
    q.Subgraphs(polygon(2)).excisions.size should equal(4)
  }

  "an octahedron with a loop" should "have a loop" in {
    val q = PlanarGraph(19, Vector(Vector(), List((10, 20), (11, 23), (16, 21), (13, 25)), List((11, 21), (12, 23), (18, 22), (14, 24)), List((10, 23), (17, 20), (15, 26), (12, 22)), List((7, 25), (8, 19), (17, 26), (13, 20)), List((7, 19), (16, 25), (14, 21), (9, 24)), List((8, 26), (9, 19), (18, 24), (15, 22))), IndexedSeq((1,0), (1,0), (1,0), (1,0), (1,0), (1,0)), 1)

    q.Subgraphs(loop).excisions.size should equal(1)
  }

  "two loops" should "have a loop" in {
    val q = PlanarGraph(1,Vector(Vector()),IndexedSeq(),2)
    q.Subgraphs(loop).excisions.size should equal(1)
  }
  
  "an inverted bowtie" should "contain one twists" in {
    val q = PlanarGraph(10, Vector(List(), List((4, 10), (5, 15), (5, 14), (4, 15))), IndexedSeq((1,0)), 0)
    val e = q.Subgraphs(PlanarGraph(6, Vector(List((2, 6), (3, 7)), List((4, 6), (4, 9), (3, 6), (2, 7))), IndexedSeq((1,0)), 0)).excisions.toList
    e.size should equal(1)
  }
  
  "a pair of nested hopf links" should "contain two hopf link" in {
    val q = PlanarGraph(13,Vector(List(), List((5,13), (6,19), (10,16), (9,14)), List((5,19), (9,13), (10,14), (6,16)), List((7,18), (8,19), (11,17), (12,15)), List((7,19), (12,18), (11,15), (8,17))),IndexedSeq((1,0), (1,0), (1,0), (1,0)),0)
    val e = q.Subgraphs(PlanarGraph(7,Vector(List(), List((3,8), (4,7), (5,9), (6,10)), List((3,7), (6,8), (5,10), (4,9))),IndexedSeq((1,0), (1,0)),0)).excisions.toList
    e.size should equal(2)
  }
  "a disjoint pair of hopf links" should "contain four hopf link" in {
    val q = PlanarGraph(13,Vector(List(), List((5,13), (6,19), (10,16), (9,14)), List((5,19), (9,13), (10,14), (6,16)), List((7,18), (8,13), (11,17), (12,15)), List((7,13), (12,18), (11,15), (8,17))),IndexedSeq((1,0), (1,0), (1,0), (1,0)),0)
    val e = q.Subgraphs(PlanarGraph(7,Vector(List(), List((3,8), (4,7), (5,9), (6,10)), List((3,7), (6,8), (5,10), (4,9))),IndexedSeq((1,0), (1,0)),0)).excisions.toList
    e.size should equal(4)
  }
  "a trefoil between two strings" should "contain a trefoil" in {
    val q = PlanarGraph(7,Vector(Vector((6,7), (6,8), (10,7), (10,23)), List((2,10), (3,7), (4,9), (5,11)), List((3,9), (2,7), (16,10), (15,24)), List((4,11), (15,9), (16,24), (5,10))),IndexedSeq((1,0), (1,0), (1,0)),0)
    val e = q.Subgraphs(PlanarGraph(10,Vector(List(), List((6,12), (9,13), (7,11), (8,14)), List((4,10), (6,13), (8,12), (5,14)), List((4,13), (5,10), (7,14), (9,11))),IndexedSeq((1,0), (1,0), (1,0)),0)).excisions.toList
    e.size should equal(2)
  }
  "a twisted H" should "contain itself" in {
    twistedH.Subgraphs(twistedH).excisions.size should equal(1)
    twistedH.Subgraphs(PlanarGraph.spider.rotate(twistedH,1)).excisions.size should equal(1)
  }
}

