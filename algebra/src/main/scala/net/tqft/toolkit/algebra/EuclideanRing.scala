package net.tqft.toolkit.algebra

trait EuclideanRig[A] extends CommutativeRig[A] {
  def quotientRemainder(x: A, y: A): (A, A)
  def quotient(x: A, y: A): A = quotientRemainder(x, y)._1
  def remainder(x: A, y: A): A = quotientRemainder(x, y)._2

  @scala.annotation.tailrec
  final def euclideanAlgorithm(x: A, y: A): A = {
    if (y == zero) {
      x
    } else {
      euclideanAlgorithm(y, remainder(x, y))
    }
  }

  def gcd(x: A, y: A): A = euclideanAlgorithm(x, y)
  def gcd(xs: A*): A = {
    xs.size match {
      case 0 => one
      case 1 => xs.head
      case _ => gcd((gcd(xs(0), xs(1)) +: xs.drop(2)): _*)
    }
  }
  def lcm(x: A, y: A): A = quotient(multiply(x, y), gcd(x, y))
  def lcm(xs: A*): A = {
    xs.size match {
      case 0 => one
      case 1 => xs.head
      case _ => lcm((lcm(xs(0), xs(1)) +: xs.drop(2)): _*)
    }
  }

  def digits(x: A, base: A = fromInt(10)): Seq[A] = {
    if (x == zero) {
      Seq.empty
    } else {
      quotientRemainder(x, base) match {
        case (q, r) => digits(q, base) :+ r
      }
    }
  }
}

trait EuclideanRing[A] extends EuclideanRig[A] with CommutativeRing[A] {
  /**
   *
   * @param x
   * @param y
   * @return (a,b,g) such that a*x + b*y == g, and g is the gcd of x and y
   */
  // FIXME tail recursive
  final def extendedEuclideanAlgorithm(x: A, y: A): (A, A, A) = {
    if (y == zero) {
      (one, zero, x)
    } else {
      val (a1, b1, g) = extendedEuclideanAlgorithm(y, remainder(x, y))
      (b1, subtract(a1, multiply(b1, quotient(x, y))), g)
    }
  }
}

object EuclideanRig {
  implicit def forgetRing[A: EuclideanRing]: EuclideanRig[A] = implicitly[EuclideanRing[A]]
}

trait EuclideanRingLowPriorityImplicits {
  // implicits inherited from a supertype are given lower priority
  // this lets us forget from OrderedField to EuclideanRing, preferring the route via Field over the route via OrderedEuclideanRing
  implicit def forgetOrderedEuclideanRing[A: OrderedEuclideanRing]: EuclideanRing[A] = implicitly[EuclideanRing[A]]  
}

object EuclideanRing extends EuclideanRingLowPriorityImplicits {
  implicit def forgetField[A: Field]: EuclideanRing[A] = implicitly[EuclideanRing[A]]
  implicit def forgetIntegerModel[A: IntegerModel]: EuclideanRing[A] = implicitly[EuclideanRing[A]]
}

