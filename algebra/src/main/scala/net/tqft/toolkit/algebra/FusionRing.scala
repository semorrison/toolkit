package net.tqft.toolkit.algebra

trait FiniteDimensionalFreeModule[A] extends Module[A, Seq[A]] {
  def coefficients: Ring[A]
  val rank: Int
  override val zero = Seq.fill(rank)(coefficients.zero)
  override def add(x: Seq[A], y: Seq[A]) = x.zip(y).map(p => coefficients.add(p._1, p._2))
  override def scalarMultiply(a: A, b: Seq[A]) = b.map(x => coefficients.multiply(a, x))
  override def negate(x: Seq[A]) = x.map(coefficients.negate)
  def basis = for (i <- 0 until rank) yield for (j <- 0 until rank) yield if (i == j) coefficients.one else coefficients.zero
}

// Usually A = Int, for a concrete fusion ring. We allow other possibilities so we can write fusion solvers, etc.
trait FusionRing[A] extends FiniteDimensionalFreeModule[A] with Rig[Seq[A]] { fr =>

  override def fromInt(x: Int) = coefficients.fromInt(x) +: Seq.fill(rank - 1)(coefficients.zero)
  override val one = fromInt(1)

  def associativityConstraints = for (x <- basis; y <- basis; z <- basis) yield subtract(multiply(x, multiply(y, z)), multiply(multiply(x, y), z))

  def verifyAssociativity = associativityConstraints.map(_ == zero).reduce(_ && _)
  def verifyIdentity = (for (x <- basis) yield x == multiply(one, x) && x == multiply(x, one)).reduce(_ && _)

  trait FusionModule extends FiniteDimensionalFreeModule[A] {
    def act(x: Seq[A], m: Seq[A]): Seq[A]
    def associativityConstraints = for (x <- fr.basis; y <- fr.basis; z <- basis) yield subtract(act(x, act(y, z)), act(fr.multiply(x, y), z))
    def verifyAssociativity = associativityConstraints.map(_ == zero).reduce(_ && _)
    def asMatrix(x: Seq[A]) = new Matrix(rank, for (b <- basis) yield act(x, b))
  }

  object regularModule extends FusionModule {
    override val rank = fr.rank
    override val coefficients = fr.coefficients
    override def act(x: Seq[A], m: Seq[A]) = fr.multiply(x, m)
  }
}

//
//trait FusionBimodule[A] extends FiniteDimensionalFreeModule[A] {
//  val leftFusionRing: FusionRing[A]
//  val rightFusionRing: FusionRing[A]
//  def act(left: Seq[A], m: Seq[A], right: Seq[A]): Seq[A]  
//}

trait FusionRingWithDimensions extends FusionRing[Int] { fr =>
  def dimensionField: RealNumberField[Int, Double]
  def dimensions: Seq[Polynomial[Fraction[Int]]]
  def dimension(x: Seq[Int]): Polynomial[Fraction[Int]] = {
    val polynomials = Polynomials.over(Gadgets.Rationals)
    import Implicits.integersAsRationals
    polynomials.add(x.zip(dimensions).map(p => polynomials.scalarMultiply(p._1, p._2)))
  }

  def dimensionBounds(x: Seq[Int]): Double = {
    import Implicits.Integers
    dimensionField.approximateWithin(0.0001)(dimension(x)) + 0.0001
  }

  def objectsSmallEnoughToBeAlgebras: Iterator[Seq[Int]] = {
    val start = Iterator(Seq(1))
    basis.tail.foldLeft(start)({
      (i: Iterator[Seq[Int]], b: Seq[Int]) =>
        i.flatMap({
          a: Seq[Int] => for (m <- 0 to dimensionBounds(b).floor.intValue) yield a :+ m
        })
    })
  }

  case class FusionMatrix(algebraObject: Seq[Int], matrix: Matrix[Int]) {
    val dimensions = {
      val dxi = fr.dimension(algebraObject)
      import Implicits.{ Integers, integersAsRationals }
      implicit val polynomials = Polynomials.over(Gadgets.Rationals)
      val Atd = matrix.transpose.mapEntries(polynomials.constant(_)).apply(fr.dimensions)
      Atd.map(p => dimensionField.quotient(p, dxi))
    }
    lazy val sortedMatrix = Matrix(matrix.numberOfColumns, matrix.entries.seq.transpose.zip(dimensions).sortBy(_._2)(dimensionField).map(_._1).transpose)
  }

  def candidateFusionMatrices: Iterator[FusionMatrix] = {
    for (
      o <- objectsSmallEnoughToBeAlgebras;
      a = regularModule.asMatrix(o);
      m <- Matrices.positiveSymmetricDecompositions(a)
    ) yield {
      FusionMatrix(o, m)
    }
  }

  def candidateAlgebraObjects: Iterator[Seq[Int]] = {
    import net.tqft.toolkit.collections.RemoveDuplicates._
    candidateFusionMatrices.map(_.algebraObject).removeDuplicates()
  }

  def candidateFusionModules: Iterator[FusionModule] = {
    val matricesBySize = candidateFusionMatrices.toList.groupBy(_.matrix.numberOfColumns)
    for (n <- (1 to matricesBySize.keys.max).iterator) yield {
      ???
    }
  }

  def candidateBrauerPicardGroupoids: Seq[Groupoid] = {
    val admissibleModules: Seq[FusionModule] = ???
    val bimodules: Seq[(FusionModule, Seq[FusionBimodule])] = ???
    ???
  }

  trait FusionModule
  trait FusionBimodule

}

trait Groupoid

object FusionRing {
  def apply[A: Ring](multiplicities: Seq[Seq[Seq[A]]]): FusionRing[A] = new StructureCoefficientFusionRing(multiplicities)
  def apply(multiplicities: Seq[Seq[Seq[Int]]], fieldGenerator: Polynomial[Int], fieldGeneratorApproximation: Double, fieldGeneratorEpsilon: Double, dimensions: Seq[Polynomial[Fraction[Int]]]): FusionRingWithDimensions = new StructureCoefficientFusionRingWithDimensions(multiplicities, fieldGenerator, fieldGeneratorApproximation, fieldGeneratorEpsilon, dimensions)

  private class StructureCoefficientFusionRing[A: Ring](multiplicities: Seq[Seq[Seq[A]]]) extends FusionRing[A] {
    override lazy val coefficients = implicitly[Ring[A]]
    override lazy val rank = multiplicities.size

    override def multiply(x: Seq[A], y: Seq[A]) = {
      val zero = coefficients.zero
      val terms = for ((xi, i) <- x.zipWithIndex; if (xi != zero); (yj, j) <- y.zipWithIndex; if (yj != zero)) yield for (k <- 0 until rank) yield coefficients.multiply(xi, yj, multiplicities(i)(j)(k))
      val result = add(terms)
      result
    }
  }

  private class StructureCoefficientFusionRingWithDimensions(multiplicities: Seq[Seq[Seq[Int]]], fieldGenerator: Polynomial[Int], fieldGeneratorApproximation: Double, fieldGeneratorEpsilon: Double, override val dimensions: Seq[Polynomial[Fraction[Int]]]) extends StructureCoefficientFusionRing[Int](multiplicities)(Gadgets.Integers) with FusionRingWithDimensions {
    override def dimensionField = {
      import Implicits.{ Integers, Doubles }
      RealNumberField(fieldGenerator, fieldGeneratorApproximation, fieldGeneratorEpsilon)(Gadgets.Integers, Gadgets.Doubles)
    }
  }
}

object Goals extends App {
  val haagerupFusionRing: FusionRingWithDimensions = {
    val haagerupMultiplicities: Seq[Seq[Seq[Int]]] =
      List(
        List(List(1, 0, 0, 0), List(0, 1, 0, 0), List(0, 0, 1, 0), List(0, 0, 0, 1)),
        List(List(0, 1, 0, 0), List(1, 2, 2, 1), List(0, 2, 1, 1), List(0, 1, 1, 1)),
        List(List(0, 0, 1, 0), List(0, 2, 1, 1), List(1, 1, 1, 1), List(0, 1, 1, 0)),
        List(List(0, 0, 0, 1), List(0, 1, 1, 1), List(0, 1, 1, 0), List(1, 1, 0, 0)))

    val haagerupFieldGenerator: Polynomial[Int] = {
      import Implicits.Integers
      Polynomial(2 -> 1, 0 -> 13)
    }
    val haagerupFieldGeneratorApproximation: Double = 3.60555
    val haagerupFieldGeneratorEpsilon: Double = 0.00001
    val haagerupDimensions: Seq[Polynomial[Fraction[Int]]] = {
      import Implicits.{ Integers, Rationals, integersAsRationals }
      Seq(
        Polynomial(0 -> Fraction(1, 1)),
        Polynomial(0 -> Fraction(5, 2), 1 -> Fraction(1, 2)),
        Polynomial(0 -> Fraction(3, 2), 1 -> Fraction(1, 2)),
        Polynomial(0 -> Fraction(1, 2), 1 -> Fraction(1, 2)))
    }
//    val haagerupDimensionBounds: Seq[Double] = Seq(1.0, 4.31, 3.31, 2.31)
    FusionRing(haagerupMultiplicities, haagerupFieldGenerator, haagerupFieldGeneratorApproximation, haagerupFieldGeneratorEpsilon, haagerupDimensions).ensuring(_.verifyAssociativity).ensuring(_.verifyIdentity)
  }
  //  println(haagerupFusionRing.regularRepresentation(Seq(1,1,0,0)))
  //  println(Matrices.positiveSymmetricDecompositions(haagerupFusionRing.regularRepresentation(Seq(1,1,0,0))).toList)

  println(haagerupFusionRing.candidateAlgebraObjects.toList)
  println(haagerupFusionRing.candidateFusionMatrices.toList.mkString("\n\n"))
  haagerupFusionRing.candidateBrauerPicardGroupoids
}