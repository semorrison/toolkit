package net.tqft.toolkit.algebra.graphs

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import org.scalatest.matchers.Matcher
import org.scalatest.matchers.MatchResult

@RunWith(classOf[JUnitRunner])
class PlanarGraphTest extends FlatSpec with Matchers with IsomorphismMatchers {
  val spider = implicitly[Spider[PlanarGraph]]

  import PlanarGraph._
  
  "a stiched strand" should "be a loop" in {
    loop should be_isomorphic_to(spider.stitch(strand))
  }

  "a rotated strand" should "be a strand" in {
    spider.rotate(strand, 1) should be_isomorphic_to(strand)
  }
  
  "a strand times a strand" should "be a strand" in {
    spider.multiply(strand, strand, 1) should be_isomorphic_to(strand)
  }
  
  "the norm of a strand" should "be a loop" in {
    spider.normSquared(strand) should be_isomorphic_to(loop)
  }

  "the two 2-strand Temperley-Lieb diagrams" should "not be isomorphic" in {
    val twoStrands = spider.tensor(strand, strand)
    twoStrands shouldNot be_isomorphic_to(spider.rotate(twoStrands, 1))
  }
  
  "a 2pi srotated trivalent vertex" should "be a trivalent vertex" in {
    star(3) should be_isomorphic_to(spider.rotate(star(3), 3))
  }
  
  "multiplying by a strand" should "do nothing" in {
    spider.multiply(strand, star(3), 1) should be_isomorphic_to(star(3)) 
  }
  
  "a pentagon" should "be rotationally invariant" in {
    polygon(5) should be_isomorphic_to(spider.rotate(polygon(5),1))
  }
  
//  println(star(3))
//  println(Seq.fill(2)(star(3)).fold(strand)(spider.multiply(_, _, 1)))
  
  "a polygon" should "be built out of trivalent vertices" in {
    val k =  5
    def p(m: Int) = spider.stitch(spider.rotate(Seq.fill(m)(star(3)).fold(strand)(spider.multiply(_, _, 1)), 1))
    
    p(k) should be_isomorphic_to(polygon(k))
  }
}

trait IsomorphismMatchers {
  class PlanarGraphIsomorphismMatcher(right: PlanarGraph) extends Matcher[PlanarGraph] {
    val spider = implicitly[Spider[PlanarGraph]]

    def apply(left: PlanarGraph) = {
      val leftCanonical = spider.canonicalForm(left)
      val rightCanonical = spider.canonicalForm(right)
      MatchResult(
        leftCanonical == rightCanonical,
        s"""Graph $left (with canonical form $leftCanonical) is not isomorphic to $right (with canonical form $rightCanonical)""",
        s"""Graph $left is isomorphic to $right"""")
    }
  }

  def be_isomorphic_to(right: PlanarGraph) = new PlanarGraphIsomorphismMatcher(right)
}
