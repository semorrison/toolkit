package net.tqft.toolkit.algebra.spiders

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import net.tqft.toolkit.algebra.polynomials.MultivariablePolynomial
import net.tqft.toolkit.algebra.Fraction
import net.tqft.toolkit.algebra.matrices2.Matrix

@RunWith(classOf[JUnitRunner])
class InnerProductsTest extends FlatSpec with Matchers with IsomorphismMatchers {
  val spider = Trivalent.TrivalentSpider

  val `D(4,0)` = TrivalentGraphs.withoutSmallFaces.byNumberOfFaces(4, 0)
  val `M(4,0)` = spider.innerProductMatrix(`D(4,0)`.toIndexedSeq)
  
//  import net.tqft.toolkit.algebra.matrices2.GaussianElimination._
//  val `Delta(4,0)` = `M(4,0)`.determinant
  
  "inner products of D(4,0)" should "be correct" in {

    val d = "d"
    val b = "b"
    val t = "t"
    val result: List[List[Fraction[MultivariablePolynomial[Fraction[Int], String]]]] = List(List(Fraction(MultivariablePolynomial(Map(Map(d -> 2) -> Fraction(1, 1))), MultivariablePolynomial(Map(Map() -> Fraction(1, 1)))), Fraction(MultivariablePolynomial(Map(Map(d -> 1) -> Fraction(1, 1))), MultivariablePolynomial(Map(Map() -> Fraction(1, 1)))), Fraction(MultivariablePolynomial(Map()), MultivariablePolynomial(Map(Map() -> Fraction(1, 1)))), Fraction(MultivariablePolynomial(Map(Map(b -> 1, d -> 1) -> Fraction(1, 1))), MultivariablePolynomial(Map(Map() -> Fraction(1, 1))))), List(Fraction(MultivariablePolynomial(Map(Map(d -> 1) -> Fraction(1, 1))), MultivariablePolynomial(Map(Map() -> Fraction(1, 1)))), Fraction(MultivariablePolynomial(Map(Map(d -> 2) -> Fraction(1, 1))), MultivariablePolynomial(Map(Map() -> Fraction(1, 1)))), Fraction(MultivariablePolynomial(Map(Map(b -> 1, d -> 1) -> Fraction(1, 1))), MultivariablePolynomial(Map(Map() -> Fraction(1, 1)))), Fraction(MultivariablePolynomial(Map()), MultivariablePolynomial(Map(Map() -> Fraction(1, 1))))), List(Fraction(MultivariablePolynomial(Map()), MultivariablePolynomial(Map(Map() -> Fraction(1, 1)))), Fraction(MultivariablePolynomial(Map(Map(b -> 1, d -> 1) -> Fraction(1, 1))), MultivariablePolynomial(Map(Map() -> Fraction(1, 1)))), Fraction(MultivariablePolynomial(Map(Map(b -> 2, d -> 1) -> Fraction(1, 1))), MultivariablePolynomial(Map(Map() -> Fraction(1, 1)))), Fraction(MultivariablePolynomial(Map(Map(t -> 1, b -> 1, d -> 1) -> Fraction(1, 1))), MultivariablePolynomial(Map(Map() -> Fraction(1, 1))))), List(Fraction(MultivariablePolynomial(Map(Map(b -> 1, d -> 1) -> Fraction(1, 1))), MultivariablePolynomial(Map(Map() -> Fraction(1, 1)))), Fraction(MultivariablePolynomial(Map()), MultivariablePolynomial(Map(Map() -> Fraction(1, 1)))), Fraction(MultivariablePolynomial(Map(Map(t -> 1, b -> 1, d -> 1) -> Fraction(1, 1))), MultivariablePolynomial(Map(Map() -> Fraction(1, 1)))), Fraction(MultivariablePolynomial(Map(Map(b -> 2, d -> 1) -> Fraction(1, 1))), MultivariablePolynomial(Map(Map() -> Fraction(1, 1))))))

    `M(4,0)` should equal(result)
  }

}

