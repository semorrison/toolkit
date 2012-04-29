package net.tqft.toolkit.algebra
import net.tqft.toolkit.mathematica.MathematicaExpression

trait Semigroup[A] {
  def multiply(x: A, y: A): A
  def power(x: A, k: Int): A = {
    require(k >= 1)
    List.fill(k)(x).reduce(multiply(_, _))
  }
}

trait One[A] {
  def one: A
}

trait Monoid[A] extends Semigroup[A] with One[A] {
  override def power(x: A, k: Int): A = {
    require(k >= 0)
    List.fill(k)(x).foldLeft(one)(multiply(_, _))
  }
}

trait Group[A] extends Monoid[A] {
  def inverse(x: A): A
}

trait CommutativeSemigroup[A] {
  def add(x: A, y: A): A
}

trait Zero[A] {
  def zero: A
}

trait CommutativeMonoid[A] extends CommutativeSemigroup[A] with Zero[A]

trait Subtractive[A] extends CommutativeSemigroup[A] {
  def negate(x: A): A
  def subtract(x: A, y: A) = add(x, negate(y))
}

trait CommutativeGroup[A] extends CommutativeMonoid[A] with Subtractive[A]

trait Category[O, M] {
  def identityMorphism(o: O): M
  def source(m: M): O
  def target(m: M): O
  def compose(x: M, y: M): M
}

trait TypedCategory[O[_], M[_, _]] {
  def identityMorphism[A](o: O[A]): M[A, A]
  def source[A, B](m: M[A, B]): O[A]
  def target[A, B](m: M[A, B]): O[B]
  def compose[A, B, C](x: M[A, B], y: M[B, C]): M[A, C]
}

trait NLinearCategory[O, M] extends Category[O, M] { nlc =>
  def zeroMorphism(o1: O, o2: O): M
  def add(x: M, y: M): M

  protected class EndomorphismRig(o: O) extends Rig[M] {
    def zero = nlc.zeroMorphism(o, o)
    def one = nlc.identityMorphism(o)
    def multiply(x: M, y: M) = nlc.compose(x, y)
    def add(x: M, y: M) = nlc.add(x, y)
  }

}

trait AdditiveCategory[O, M] extends NLinearCategory[O, M] with Subtractive[M] { ac =>
  protected class EndomorphismRing(o: O) extends EndomorphismRig(o) with Ring[M] {
    def negate(x: M) = ac.negate(x)
  }

  def endomorphismRing(o: O): Ring[M] = new EndomorphismRing(o)
}

trait LinearCategory[O, M, R] extends AdditiveCategory[O, M] { lc =>
  def scalarMultiply(r: R, m: M): M

  protected class EndomorphismAlgebra(o: O) extends EndomorphismRing(o) with Algebra[R, M] {
    def scalarMultiply(a: R, b: M) = lc.scalarMultiply(a, b)
  }

  def endomorphismAlgebra(o: O): Algebra[R, M] = new EndomorphismAlgebra(o)
}

trait Rig[A] extends NLinearCategory[Unit, A] with Monoid[A] with CommutativeMonoid[A] {
  override def identityMorphism(o: Unit) = one
  override def source(a: A) = ()
  override def target(a: A) = ()
  override def compose(x: A, y: A) = multiply(x, y)
  override def zeroMorphism(o1: Unit, o2: Unit): A = zero

  def fromInt(x: Int) = {
    import AlgebraicNotation._
    implicit val hoc = this

    x match {
      case x if x < 0 => throw new UnsupportedOperationException
      case 0 => zero
      case x if x > 0 => List.fill(x)(one) reduceLeft (_ + _)
    }
  }

}

trait Ring[A] extends Rig[A] with AdditiveCategory[Unit, A] with CommutativeGroup[A] {
  def multiply(x: A, y: Int): A = multiply(x, fromInt(y))
  override def fromInt(x: Int) = {
    import AlgebraicNotation._
    implicit val hoc = this

    x match {
      case x if x < 0 => -(List.fill(-x)(one) reduceLeft (_ + _))
      case _ => super.fromInt(x)
    }
  }

}

trait RingHomomorphism[A, B] extends Homomorphism[Ring, A, B]

trait CommutativeRing[A] extends Ring[A]

trait EuclideanDomain[A] extends CommutativeRing[A] {
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

  /**
   *
   * @param x
   * @param y
   * @return (a,b,g) such that a*x + b*y == g, and g is the gcd of x and y
   */
  final def extendedEuclideanAlgorithm(x: A, y: A): (A, A, A) = {
    if (y == zero) {
      (one, zero, x)
    } else {
      val (a1, b1, g) = extendedEuclideanAlgorithm(y, remainder(x, y))
      (b1, subtract(a1, multiply(b1, quotient(x, y))), g)
    }
  }

  def gcd(x: A, y: A): A = euclideanAlgorithm(x, y) // ensuring { _ == extendedEuclideanAlgorithm(x, y)._3 }
}

trait OrderedEuclideanDomain[A] extends EuclideanDomain[A] with Ordering[A] {
  def signum(x: A): A = compare(x, zero) match {
    case 0 => zero
    case x if x < 0 => negate(one)
    case _ => one
  }

  override def gcd(x: A, y: A) = {
    val gcd = super.gcd(x, y)
    multiply(gcd, multiply(signum(gcd), signum(y)))
  }
}

trait WithInverses[A] {
  def inverse(x: A): A
}

trait Field[A] extends EuclideanDomain[A] with WithInverses[A] {
  override def quotientRemainder(x: A, y: A) = (multiply(x, inverse(y)), zero)
  override def remainder(x: A, y: A) = zero
}

trait OrderedField[A] extends Field[A] with Ordering[A] { self =>
  def abs(x: A): A = {
    signum(x) match {
      case s if s >= 0 => x
      case s if s < 0 => negate(x)
    }
  }
  def signum(x: A) = compare(x, zero)
  def chop(x: A, epsilon: A): A = {
    if (compare(abs(x), epsilon) < 0) zero else x
  }
}

trait ApproximateField[A] extends OrderedField[A] {
  /* a small quantity, but 1 and 1+epsilon are still distinguishable */
  def epsilon: A
  def chop(x: A): A = chop(x, epsilon)
  def close(x: A, y: A) = {
    if (chop(x) == zero) {
      chop(y) == zero
    } else if (chop(y) == zero) {
      chop(x) == zero
    } else {
      chop(subtract(quotient(x, y), one)) == zero
    }
  }

  // TODO this should probably use epsilon to decide to fixed point
  def sqrt(x: A): A = {
    import net.tqft.toolkit.functions.FixedPoint
    val initialGuess = one //new BigDecimal(x.underlying.scaleByPowerOfTen(-x.scale / 2), x.mc)
    val result = abs(FixedPoint.sameTest(close _)({ g: A => quotient(add(quotient(x, g), g), fromInt(2)) })(initialGuess))
    result
  }

  import collection.generic.CanBuildFrom
  import collection.TraversableLike

  def norm[CC[X] <: TraversableLike[X, CC[X]]](v: CC[A]): A = {
    if (v.isEmpty) {
      zero
    } else {
      sqrt(v.map(power(_, 2)).reduceLeft(add(_, _)))
    }
  }

  def normalize[CC[X] <: TraversableLike[X, CC[X]]](v: CC[A])(implicit cbf1: CanBuildFrom[CC[A], Int, CC[Int]], cbf2: CanBuildFrom[CC[A], A, CC[A]]): CC[A] = {
    val s = v.map(signum(_)).filter(_ != 0).head
    val n = multiply(norm(v), s)
    v map { x => quotient(x, n) }
  }

}

trait ApproximateReals[A] extends ApproximateField[A] {
  def fromDouble(x: Double): A
  def fromBigDecimal(x: BigDecimal): A
  def setPrecision(x: A): A
  def bigDecimalValue(x: A): BigDecimal
}

trait FieldHomomorphism[A, B] extends Homomorphism[Field, A, B]

object Functors {

  type Pair[A] = (A, A)
  type Identity[A] = A

  def Forget[O1[A] <: O2[A], O2[_]](cat1: HomomorphismCategory[O1], cat2: HomomorphismCategory[O2]): Functor[O1, O2, Identity] = new Functor[O1, O2, Identity] {
    def source = cat1
    def target = cat2
    def apply[A](o: O1[A]) = o
    def apply[A, B](hom: Homomorphism[O1, A, B]) = hom
  }

  def Identity[O[_]](cat: HomomorphismCategory[O]): Endofunctor[O, Identity] = new Endofunctor[O, Identity] {
    def source = cat
    def target = cat
    def apply[A](o: O[A]) = o
    def apply[A, B](hom: Homomorphism[O, A, B]) = hom
  }

  object Rings {
    def matricesOver(size: Int) = Matrices.matricesOver(size)
  }

  object EuclideanDomains {
    def fieldOfFractions = net.tqft.toolkit.algebra.Fields.fieldOfFractions
  }

  object Fields {

  }
}

trait Homomorphism[+O[_], A, B] extends (A => B) {
  def source: O[A]
  def target: O[B]
}

trait GeneralFunctor[O1[_], H1[A, B] <: Homomorphism[O1, A, B], O2[_], H2[A, B] <: Homomorphism[O2, A, B], Q[_]] { outer =>
  def source: GeneralHomomorphismCategory[O1, H1]
  def target: GeneralHomomorphismCategory[O2, H2]
  def apply[A](a: O1[A]): O2[Q[A]]
  def apply[A, B](hom: H1[A, B]): H2[Q[A], Q[B]]

  def andThen[O3[_], H3[A, B] <: Homomorphism[O3, A, B], P[_]](f: GeneralFunctor[O2, H2, O3, H3, P]): GeneralFunctor[O1, H1, O3, H3, ({ type PQ[A] = P[Q[A]] })#PQ] = {
    type PQ[A] = P[Q[A]]
    new GeneralFunctor[O1, H1, O3, H3, PQ] {
      def source = outer.source
      def target = f.target
      def apply[A](a: O1[A]) = f.apply(outer.apply(a))
      def apply[A, B](hom: H1[A, B]) = f.apply(outer.apply(hom))
    }
  }
}

trait Functor[O1[_], O2[_], Q[_]] extends GeneralFunctor[O1, ({ type Hom[A, B] = Homomorphism[O1, A, B] })#Hom, O2, ({ type Hom[A, B] = Homomorphism[O2, A, B] })#Hom, Q] {
}

trait GeneralEndofunctor[O[_], H[A, B] <: Homomorphism[O, A, B], Q[_]] extends GeneralFunctor[O, H, O, H, Q]

trait Endofunctor[O[_], Q[_]] extends GeneralEndofunctor[O, ({ type Hom[A, B] = Homomorphism[O, A, B] })#Hom, Q]

trait GeneralNaturalTransformation[O1[_], H1[B, C] <: Homomorphism[O1, B, C], O2[_], H2[B, C] <: Homomorphism[O2, B, C], P[_], Q[_]] {
  def source: GeneralFunctor[O1, H1, O2, H2, P]
  def target: GeneralFunctor[O1, H1, O2, H2, Q]
  def apply[A](o: O1[A]): H2[P[A], Q[A]]
}

trait NaturalTransformation[O1[_], O2[_], P[_], Q[_]] extends GeneralNaturalTransformation[O1, ({ type Hom[A, B] = Homomorphism[O1, A, B] })#Hom, O2, ({ type Hom[A, B] = Homomorphism[O2, A, B] })#Hom, P, Q] {
}

trait GeneralHomomorphismCategory[O[_], H[A, B] <: Homomorphism[O, A, B]] extends TypedCategory[O, H] {
  def homomorphismFactory[A, B](source: O[A], target: O[B], function: A => B): H[A, B]

  def compose[A, B, C](x: H[A, B], y: H[B, C]) = homomorphismFactory(source = x.source, target = y.target, function = { a: A => y(x(a)) })
  def target[A, B](x: H[A, B]) = x.target
  def source[A, B](x: H[A, B]) = x.source
  def identityMorphism[A](o: O[A]) = homomorphismFactory(source = o, target = o, function = { a: A => a })
}

trait HomomorphismCategory[O[_]] extends GeneralHomomorphismCategory[O, ({ type H[A, B] = Homomorphism[O, A, B] })#H] {
  override def homomorphismFactory[A, B](_source: O[A], _target: O[B], function: A => B) = new Homomorphism[O, A, B] {
    val source = _source
    val target = _target
    def apply(a: A) = function(a)
  }
}

object Rings extends HomomorphismCategory[Ring] {
  val Functors = net.tqft.toolkit.algebra.Functors.Rings
}

trait Module[A, B] extends CommutativeGroup[B] {
  def scalarMultiply(a: A, b: B): B
}

trait Algebra[A, B] extends Ring[B] with Module[A, B]

trait AssociativeAlgebra[A, B] extends Algebra[A, B]

trait PolynomialAlgebra[A] extends FreeModuleOnMonoid[A, Int, Polynomial[A]] with AssociativeAlgebra[A, Polynomial[A]] {

  def monomial(k: Int): Polynomial[A] = monomial(k, ring.one)
  def monomial(k: Int, a: A): Polynomial[A] = Polynomial((k, a))

  def constant(a: A) = monomial(0, a)

  override val monoid = Gadgets.Integers

  override def wrap(terms: List[(Int, A)]): Polynomial[A] = new PolynomialImpl(terms)
  private class PolynomialImpl(_terms: List[(Int, A)]) extends Polynomial[A] {
    val terms = reduce(_terms)
  }

}

trait PolynomialAlgebraOverField[A] extends PolynomialAlgebra[A] with EuclideanDomain[Polynomial[A]] {
  override implicit def ring: Field[A]

  def quotientRemainder(x: Polynomial[A], y: Polynomial[A]): (Polynomial[A], Polynomial[A]) = {
    (x.maximumDegree, y.maximumDegree) match {
      case (_, None) => throw new ArithmeticException
      case (None, Some(dy)) => (Polynomial(), Polynomial())
      case (Some(dx), Some(dy)) => {
        if (dy > dx) {
          (Polynomial(), x)
        } else {
          val ax = x.leadingCoefficient.get
          val ay = y.leadingCoefficient.get

          require(ax != ring.zero)
          require(ay != ring.zero)

          val q = ring.quotient(ax, ay)

          val quotientLeadingTerm = monomial(dx - dy, q)
          val difference = add(x, negate(multiply(quotientLeadingTerm, y)))
          require(difference.get(dx) == None)
          val (restOfQuotient, remainder) = quotientRemainder(difference, y)

          (add(quotientLeadingTerm, restOfQuotient), remainder)
        }
      }
    }
  }

}