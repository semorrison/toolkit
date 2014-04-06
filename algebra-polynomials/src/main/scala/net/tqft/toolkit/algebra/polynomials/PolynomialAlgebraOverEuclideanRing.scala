package net.tqft.toolkit.algebra.polynomials

import net.tqft.toolkit.algebra.EuclideanRing

trait PolynomialAlgebraOverEuclideanRing[A, P] extends PolynomialAlgebra[A, P] with EuclideanRing[P] {
  override def ring: EuclideanRing[A]
  override def quotientRemainder(x: P, y: P): (P, P) = {
    (maximumDegree(x), maximumDegree(y)) match {
      case (_, None) => throw new ArithmeticException
      case (None, Some(dy)) => (zero, zero)
      case (Some(dx), Some(dy)) => {
        if (dy > dx) {
          (zero, x)
        } else {
          val ax = leadingCoefficient(x).get
          val ay = leadingCoefficient(y).get

          require(ax != ring.zero)
          require(ay != ring.zero)

          val q = ring.quotient(ax, ay)

          val quotientLeadingTerm = monomial(dx - dy, q)
          val difference = add(x, negate(multiply(quotientLeadingTerm, y)))
          val (restOfQuotient, remainder) = quotientRemainder(difference, y)

          (add(quotientLeadingTerm, restOfQuotient), remainder)
        }
      }
    }
  }

  def squareFreeFactorization(p: P): Stream[P] = {
    maximumDegree(p) match {
      case None | Some(0) => Stream(p)
      case _ => gcd(p, formalDerivative(p)) match {
        case q if maximumDegree(q).get == 0 => Stream(q)
        case q => q #:: squareFreeFactorization(quotient(p, q))
      }
    }
  }

  def removeMultipleRoots(p: P): P = squareFreeFactorization(p).head

  def sturmSequence(p: P): Stream[P] = {
    def stream: Stream[P] = p #:: formalDerivative(p) #:: (stream.zip(stream.tail).map({
      case (a, b) => negate(remainder(a, b))
    }))
    stream.takeWhile(q => maximumDegree(q).nonEmpty)
  }
}

object PolynomialAlgebraOverEuclideanRing {
  trait PolynomialAlgebraOverEuclideanRingForMaps[A] extends PolynomialAlgebra.PolynomialAlgebraForMaps[A] with PolynomialAlgebraOverEuclideanRing[A, Map[Int, A]] {
    override def ring: EuclideanRing[A]
  }
  trait PolynomialAlgebraOverEuclideanRingForPolynomials[A] extends PolynomialAlgebra.PolynomialAlgebraForPolynomials[A] with PolynomialAlgebraOverEuclideanRing[A, Polynomial[A]] {
    override def ring: EuclideanRing[A]
  }

  implicit def forMaps[A: EuclideanRing]: PolynomialAlgebraOverEuclideanRing[A, Map[Int, A]] = new PolynomialAlgebraOverEuclideanRingForMaps[A] {
    override def ring = implicitly[EuclideanRing[A]]
  }
  implicit def over[A: EuclideanRing]: PolynomialAlgebraOverEuclideanRing[A, Polynomial[A]] = new PolynomialAlgebra.PolynomialAlgebraForPolynomials[A] with PolynomialAlgebraOverEuclideanRing[A, Polynomial[A]] {
    override def ring = implicitly[EuclideanRing[A]]
  }
}

trait PolynomialsOverEuclideanRing[A] extends PolynomialAlgebraOverEuclideanRing[A, Polynomial[A]] with Polynomials[A]
object PolynomialsOverEuclideanRing {
  implicit def over[A: EuclideanRing]: PolynomialsOverEuclideanRing[A] = new PolynomialAlgebraOverEuclideanRing.PolynomialAlgebraOverEuclideanRingForPolynomials[A] with PolynomialsOverEuclideanRing[A] {
    override def ring = implicitly[EuclideanRing[A]]
  }
}


