package net.tqft.toolkit.algebra.spiders.examples

import net.tqft.toolkit.algebra.polynomials.Polynomial
import net.tqft.toolkit.algebra.numberfields.NumberField
import net.tqft.toolkit.algebra.Fraction
import net.tqft.toolkit.algebra.spiders._


object `H3` extends CubicSpider[Polynomial[Fraction[Int]]]()(NumberField[Fraction[Int]](Seq(-13, 0, 1))) {
  override val omega = ring.one
  override val d: Polynomial[Fraction[Int]] = Seq(Fraction(3, 2), Fraction(1, 2))
  override val b: Polynomial[Fraction[Int]] = ???
  override val t: Polynomial[Fraction[Int]] = ???

  private lazy val pentapentAndHexapentReductions: Seq[Reduction[PlanarGraph, Polynomial[Fraction[Int]]]] = {
    val preSpider = Spiders.cubic(omega, d, b, t)(ring)
    val pentapentReduction = preSpider.basis(7, ???).deriveNewRelations(9).next
    val hexapentReduction = preSpider.basis(8, ???).deriveNewRelations(10).next
    Seq(pentapentReduction, hexapentReduction)
  }
  override def reductions = super.reductions ++ pentapentAndHexapentReductions
}

