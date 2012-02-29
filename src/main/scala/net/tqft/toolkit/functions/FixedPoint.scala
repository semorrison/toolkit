package net.tqft.toolkit.functions

object FixedPoint {

  implicit def fixedPointForFunction[A](f: A => A) = new FunctionFixedPoint(f)

  class FunctionFixedPoint[A](f: A => A) {
    def fixedPoint(a: A): A = {
      val iterates = Stream.iterate(a)(f)
      ((iterates zip iterates.tail) find { case (a, b) => a == b }).get._1
    }
  }

  def apply[A](f: A => A): A => A = f.fixedPoint(_)

}