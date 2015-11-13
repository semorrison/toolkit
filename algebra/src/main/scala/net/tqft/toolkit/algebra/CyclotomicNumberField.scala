package net.tqft.toolkit.algebra

import net.tqft.toolkit.algebra.polynomials.Polynomial

trait ComplexConjugation[A] { self: Field[A] =>
  def bar(q: A): A
}

trait CyclotomicNumberField[A] extends NumberField[A] with ComplexConjugation[Polynomial[A]] {
  val order: Int
  override lazy val generator = Polynomial.cyclotomic(order)(coefficientField) // has to be lazy so coefficentField is available

  private def zeta = Polynomial.identity(coefficientField)
  private val zetaInversePowers = {
    import net.tqft.toolkit.functions.Memo._
    def f(n: Int) = inverse(power(zeta, n))
    (f _).memo
  }

  def bar(q: Polynomial[A]) = add(normalize(q).terms.map({ case (k, a) => scalarMultiply(a, zetaInversePowers(k)) }))
}