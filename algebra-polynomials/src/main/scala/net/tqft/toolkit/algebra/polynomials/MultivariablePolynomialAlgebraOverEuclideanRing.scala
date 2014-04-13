package net.tqft.toolkit.algebra.polynomials

import net.tqft.toolkit.algebra._

trait MultivariablePolynomialAlgebraOverEuclideanRing[A, V] extends MultivariablePolynomialAlgebra[A, V] with GCDRing[MultivariablePolynomial[A, V]] {
  override implicit def ring: EuclideanRing[A]

  override def gcd(x: MultivariablePolynomial[A, V], y: MultivariablePolynomial[A, V]): MultivariablePolynomial[A, V] = {
    import net.tqft.toolkit.arithmetic.MinMax._
    variablesByMaximumDegree(x).headOption.orElse(variablesByMaximumDegree(y).headOption).map(_._2.head) match {
      case None => {
        if (x.coefficients.isEmpty && y.coefficients.isEmpty) {
          one
        } else if (x.coefficients.isEmpty || y.coefficients.isEmpty) {
          x.coefficients ++ y.coefficients
        } else {
          ring.gcd(x.coefficients(Map()), y.coefficients(Map()))
        }
      }
      case Some(v) => {
        implicit val polynomials = this
        val rationalFunctions = implicitly[Ring[MultivariableRationalFunction[A, V]]]
        val univariatePolynomialsInMultivariablePolynomials = implicitly[PolynomialsOverGCDRing[MultivariablePolynomial[A, V]]]
        val univariatePolynomialsInMultivariableRationalFunctions = implicitly[PolynomialsOverFieldOfFractions[MultivariablePolynomial[A, V]]]

        val xp = asUnivariatePolynomialInVariable(v)(x)
        val yp = asUnivariatePolynomialInVariable(v)(y)

        def verifyResult(r: MultivariablePolynomial[A, V]): Boolean = {
          multiply(r, exactQuotient(x, r)) == x &&
            multiply(r, exactQuotient(y, r)) == y
        }

        if ((variables(x) ++ variables(y)).size == 1) {
          // actually, there are no more variables in the coefficient functions, switch to univariate gcd
          val xo: Polynomial[A] = xp.coefficients.mapValues(p => constantTerm(p))
          val yo: Polynomial[A] = yp.coefficients.mapValues(p => constantTerm(p))

          val univariatePolynomials = implicitly[PolynomialsOverGCDRing[A]]
          val univariateGCD = univariatePolynomials.gcd(xo, yo)
          fromUnivariatePolynomialInVariable(v)(univariateGCD.coefficients.mapValues(a => MultivariablePolynomial.constant[A, V](a))) //.ensuring(verifyResult _)
        } else {

          fromUnivariatePolynomialInVariable(v)(univariatePolynomialsInMultivariablePolynomials.gcd(xp, yp)) //.ensuring(verifyResult _)

          //          // TODO this probably shouldn't lift the coefficients to fractions: the subresultant_gcd algorithm is more efficient
          //
          //          val xc = univariatePolynomialsInMultivariablePolynomials.content(xp)
          //          val yc = univariatePolynomialsInMultivariablePolynomials.content(yp)
          //
          //          val xv = xp.coefficients.mapValues(p => Fraction.whole(p)(polynomials))
          //          val yv = yp.coefficients.mapValues(p => Fraction.whole(p)(polynomials))
          //
          //          val gcdOverRationalFunctions = univariatePolynomialsInMultivariableRationalFunctions.gcd(xv, yv)
          //          val primitivePart = univariatePolynomialsInMultivariableRationalFunctions.primitivePartOverFractions(gcdOverRationalFunctions)
          //
          //          val contentGCD = gcd(xc, yc)
          //
          //          multiply(
          //            contentGCD,
          //            fromUnivariatePolynomialInVariable(v)(primitivePart)) //.ensuring(verifyResult _)
        }
      }
    }
  }

  override def exactQuotientOption(x: MultivariablePolynomial[A, V], y: MultivariablePolynomial[A, V]): Option[MultivariablePolynomial[A, V]] = {
    import net.tqft.toolkit.arithmetic.MinMax._
    variablesByMaximumDegree(x).headOption.orElse(variablesByMaximumDegree(y).headOption).map(_._2.head) match {
      case None => {
        if (y.coefficients.isEmpty) {
          throw new ArithmeticException
        } else {
          if (x.coefficients.isEmpty) {
            Some(zero)
          } else {
            ring.exactQuotientOption(x.coefficients(Map()), y.coefficients(Map())).map({ x => x })
          }
        }
      }
      case Some(v) => {
        implicit val polynomials = this

        val univariatePolynomialsInMultivariablePolynomials = implicitly[PolynomialsOverGCDRing[MultivariablePolynomial[A, V]]]

        univariatePolynomialsInMultivariablePolynomials.exactQuotientOption(
          asUnivariatePolynomialInVariable(v)(x),
          asUnivariatePolynomialInVariable(v)(y)).map(fromUnivariatePolynomialInVariable(v))

        // it is not clear to me that this is a good method
        //        val univariatePolynomialsInMultivariableRationalFunctions = implicitly[PolynomialsOverFieldOfFractions[MultivariablePolynomial[A, V]]]
        //
        //        val xp = asUnivariatePolynomialInVariable(v)(x).coefficients.mapValues(p => Fraction.whole(p)(polynomials))
        //        val yp = asUnivariatePolynomialInVariable(v)(y).coefficients.mapValues(p => Fraction.whole(p)(polynomials))
        //
        //        univariatePolynomialsInMultivariableRationalFunctions.exactQuotientOption(xp, yp).flatMap({ p =>
        //          if (p.coefficients.values.forall(_.denominator == one)) {
        //            Some(fromUnivariatePolynomialInVariable(v)(p.coefficients.mapValues(_.numerator)))
        //          } else {
        //            None
        //          }
        //        })
      }
    }
  }

}

object MultivariablePolynomialAlgebraOverEuclideanRing {
  implicit def over[A: EuclideanRing, V: Ordering]: MultivariablePolynomialAlgebraOverEuclideanRing[A, V] = new MultivariablePolynomialAlgebraOverEuclideanRing[A, V] with MultivariablePolynomialAlgebraOverRig.LexicographicOrdering[A, V] {
    override val variableOrdering = implicitly[Ordering[V]]
    override val ring = implicitly[EuclideanRing[A]]
  }
}
