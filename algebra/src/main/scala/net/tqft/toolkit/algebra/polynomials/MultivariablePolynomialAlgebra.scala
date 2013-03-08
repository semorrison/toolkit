package net.tqft.toolkit.algebra.polynomials

import net.tqft.toolkit.algebra._
import net.tqft.toolkit.algebra.modules._

trait MultivariablePolynomialAlgebraOverRig[A, V]
  extends MapFreeModuleOnMonoidOverRig[A, Map[V, Int], MultivariablePolynomial[A, V]] {

  val monomialOrdering: Ordering[Map[V, Int]]

  override def unsafeWrap(m: Map[Map[V, Int], A]): MultivariablePolynomial[A, V] = {
    m match {
      case m: MultivariablePolynomial[A, V] => m
      case m => MultivariablePolynomialImpl(m)
    }
  }
  override def wrap(m: Map[Map[V, Int], A]): MultivariablePolynomial[A, V] = {
    m match {
      case m: MultivariablePolynomial[A, V] => m
      case m => MultivariablePolynomialImpl(m.filterNot(_._2 == ring.zero))
    }
  }
  private case class MultivariablePolynomialImpl(toMap: Map[Map[V, Int], A]) extends MultivariablePolynomial[A, V]

  def monomial(v: V): MultivariablePolynomial[A, V] = MultivariablePolynomialImpl(Map(Map(v -> 1) -> ring.one))
  override def monomial(m: Map[V, Int]): MultivariablePolynomial[A, V] = MultivariablePolynomialImpl(Map(m -> ring.one))
  override def monomial(m: Map[V, Int], a: A): MultivariablePolynomial[A, V] = {
    if (a == ring.zero) {
      MultivariablePolynomialImpl(Map.empty)
    } else {
      MultivariablePolynomialImpl(Map(m -> a))
    }
  }

  override object monoid extends AdditiveMonoid[Map[V, Int]] {
    override val zero = Map.empty[V, Int]
    override def add(x: Map[V, Int], y: Map[V, Int]): Map[V, Int] = {
      if (x.size == 0) {
        y
      } else if (y.size == 0) {
        x
      } else if (x.size == 1) {
        x.head match {
          case (v, k) => {
            if (y.contains(v)) {
              y + (v -> (y(v) + k))
            } else {
              y + (v -> k)
            }
          }
        }
      } else if (y.size == 1) {
        y.head match {
          case (v, k) => {
            if (x.contains(v)) {
              x + (v -> (x(v) + k))
            } else {
              x + (v -> k)
            }
          }
        }
      } else {
        (for (k <- x.keySet ++ y.keySet) yield {
          k -> (x.getOrElse(k, 0) + y.getOrElse(k, 0))
        }).toMap
      }
    }
  }

  def substitute(values: Map[V, MultivariablePolynomial[A, V]])(p: MultivariablePolynomial[A, V]) = substitute_(values, p)
  //  def substitute(values: Map[V, MultivariablePolynomial[A, V]])(p: MultivariablePolynomial[A, V]) = substituteCache_(values, p)
  //  private val substituteCache_ = {
  //    import net.tqft.toolkit.functions.Memo
  //    Memo.softly(substitute_ _)
  //  }
  private def substitute_(values: Map[V, MultivariablePolynomial[A, V]], p: MultivariablePolynomial[A, V]): MultivariablePolynomial[A, V] = {
    val relevantValues = values.filterKeys(p.variables.contains)
    if (relevantValues.isEmpty) {
      p
    } else {
      sum(for ((m, a) <- p.toMap.iterator) yield {
        val (toReplace, toKeep) = m.keySet.partition(v => relevantValues.contains(v))
        if (toReplace.isEmpty) {
          monomial(m, a)
        } else {
          val newFactors = for (v <- toReplace.toSeq) yield power(relevantValues(v), m(v))
          if (toKeep.isEmpty) {
            scalarMultiply(a, product(newFactors))
          } else {
            val oldFactor = monomial(toKeep.map(v => v -> m(v)).toMap, a)
            multiply(oldFactor, newFactors: _*)
          }
        }
      })
    }
  }
  def substituteConstants(values: Map[V, A])(p: MultivariablePolynomial[A, V]): MultivariablePolynomial[A, V] = {
    substitute(values.mapValues(constant))(p)
  }
  def completelySubstituteConstants(values: V =>? A)(p: MultivariablePolynomial[A, V]): A = {
    val valuesWithZero = values.lift.andThen(_.getOrElse(ring.zero))

    ring.sum(for ((m, a) <- p.toMap.iterator) yield {
      ring.multiply(a, ring.product(for ((v, k) <- m) yield {
        ring.power(valuesWithZero(v), k)
      }))
    })
  }

}

trait MultivariablePolynomialAlgebra[A, V]
  extends MultivariablePolynomialAlgebraOverRig[A, V]
  with MapFreeModuleOnMonoid[A, Map[V, Int], MultivariablePolynomial[A, V]]

object MultivariablePolynomialAlgebra {
  private abstract class WithLexicographicOrdering[A: Rig, V: Ordering] extends MultivariablePolynomialAlgebraOverRig[A, V] {
    override val monomialOrdering = {
      import net.tqft.toolkit.collections.LexicographicOrdering._
      implicitly[Ordering[Map[V, Int]]]
    }
  }

  def overRig[A: Rig, V: Ordering]: MultivariablePolynomialAlgebraOverRig[A, V] = new WithLexicographicOrdering[A, V] {
    override val ring = implicitly[Rig[A]]
  }
  implicit def over[A: Ring, V: Ordering]: MultivariablePolynomialAlgebra[A, V] = new WithLexicographicOrdering[A, V] with MultivariablePolynomialAlgebra[A, V] {
    override val ring = implicitly[Ring[A]]
  }
}
