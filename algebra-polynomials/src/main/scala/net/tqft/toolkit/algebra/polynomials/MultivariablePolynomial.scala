package net.tqft.toolkit.algebra.polynomials

import net.tqft.toolkit.algebra._

import scala.language.implicitConversions

case class MultivariablePolynomial[A, V](coefficients: Map[Map[V, Int], A]) {
  require(coefficients.valuesIterator.forall(_.toString != "0"))
  require(coefficients.keysIterator.forall(_.valuesIterator.forall(_ >= 0)))
}

object MultivariablePolynomial {
  implicit def constant[A, V](a: A): MultivariablePolynomial[A, V] = MultivariablePolynomial(Map(Map.empty -> a))

  implicit def lift[A, V](coefficients: Map[Map[V, Int], A]) = MultivariablePolynomial[A, V](coefficients)

  implicit def constantRationalFunction[A: Field, V: Ordering](a: A): MultivariableRationalFunction[A, V] = MultivariablePolynomial[A, V](Map(Map.empty -> a))
  implicit def constantToFractionRationalFuncation[A: EuclideanRing, V: Ordering](a: A): MultivariableRationalFunction[Fraction[A], V] = constantRationalFunction(a)
  implicit def liftToRationalFunction[A: Field, V: Ordering](coefficients: Map[Map[V, Int], A]): MultivariableRationalFunction[A, V] = lift(coefficients)
  implicit def liftCoefficientsToFractions[A: EuclideanRing, V: Ordering](coefficients: Map[Map[V, Int], A]): MultivariableRationalFunction[Fraction[A], V] = liftToRationalFunction(coefficients.mapValues(a => (a: Fraction[A])))
  
  implicit class RichMultivariablePolynomial[A, V](m: MultivariablePolynomial[A, V]) {
    def variables = m.coefficients.keySet.flatMap(_.keySet)
    def constantTerm(implicit ring: Ring[A]): A = m.coefficients.get(Map.empty).getOrElse(implicitly[Ring[A]].zero)
    def totalDegree = {
      import net.tqft.toolkit.arithmetic.MinMax._
      m.coefficients.keys.map(_.values.sum).maxOption
    }
    def termsOfDegree(k: Int) = m.coefficients.filterKeys(_.values.sum == k)
  }

  implicit def multivariablePolynomialAlgebraAsRig[A: Rig, V: Ordering]: Rig[MultivariablePolynomial[A, V]] = implicitly[MultivariablePolynomialAlgebraOverRig[A, V]]
  implicit def multivariablePolynomialAlgebraAsRing[A: Ring, V: Ordering]: Ring[MultivariablePolynomial[A, V]] = implicitly[MultivariablePolynomialAlgebra[A, V]]
  implicit def multivariablePolynomialAlgebraAsEuclideanRing[A: Field, V: Ordering]: EuclideanRing[MultivariablePolynomial[A, V]] = implicitly[MultivariablePolynomialAlgebraOverField[A, V]]
  implicit def multivariablePolynomialAlgebraAsOrderedEuclideanRing[A: OrderedField, V: Ordering]: OrderedEuclideanRing[MultivariablePolynomial[A, V]] = implicitly[MultivariablePolynomialAlgebraOverOrderedField[A, V]]
}
