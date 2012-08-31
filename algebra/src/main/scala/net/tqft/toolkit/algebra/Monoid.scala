package net.tqft.toolkit.algebra

import scala.collection.GenSeq

trait One[A] {
  def one: A
}

trait Monoid[@specialized(Int, Long, Float, Double) A] extends Semigroup[A] with One[A] {
  def multiply(xs: GenSeq[A]): A = xs.fold(one)(multiply _)
  override def power(x: A, k: Int): A = {
    // modified from https://github.com/dlwh/breeze/blob/master/math/src/main/scala/breeze/numerics/IntMath.scala
    // under http://www.apache.org/licenses/LICENSE-2.0
    require(k >= 0)
    var b = x
    var e = k
    var result = one
    while (e != 0) {
      if ((e & 1) != 0) {
        result = multiply(result, b)
      }
      e >>= 1
      b = multiply(b, b)
    }

    result
  }
  def orderOfElement(a: A): Int = Iterator.iterate(a)(multiply(_, a)).indexOf(one) + 1
}
