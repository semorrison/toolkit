package net.tqft.toolkit.algebra

import scala.collection.GenTraversableOnce

// TODO should have
//		@specialized(Int, Long, Float, Double) 
// but this crashes the compiler (somewhere in FiniteGroups??).
// Fixed by Paul Phillips, c.f. https://issues.scala-lang.org/browse/SI-6301 but hasn't hit 2.10 yet.
trait AdditiveSemigroup[A] {
  def add(x: A, y: A): A
  final def add(x0: A, x1: A, x2: A*): A = x2.fold(add(x0, x1))(add _)
}

// TODO should have
//	    @specialized(Int, Long, Float, Double) 
// but this causes Gadgets.Integers.zero to stack overflow!
// Also fixed by Paul, see above.
trait Zero[A] {
  def zero: A
}

trait AdditiveMonoid[@specialized(Int, Long, Float, Double) A] extends AdditiveSemigroup[A] with Zero[A] {
  def sum(xs: GenTraversableOnce[A]): A = xs.reduceOption(add _).getOrElse(zero)
}

object AdditiveMonoid {
  implicit def forget[A:Rig]: AdditiveMonoid[A] = implicitly[AdditiveMonoid[A]]
}

trait Subtractive[@specialized(Int, Long, Float, Double) A] extends AdditiveSemigroup[A] {
  def negate(x: A): A
  def subtract(x: A, y: A) = add(x, negate(y))
}

trait AdditiveGroup[@specialized(Int, Long, Float, Double) A] extends AdditiveMonoid[A] with Subtractive[A]

object AdditiveGroup {
  implicit def forget[A:Ring]: AdditiveGroup[A] = implicitly[AdditiveGroup[A]]  
}