package net.tqft.toolkit.algebra.fusion

import net.tqft.toolkit.algebra._
import net.tqft.toolkit.algebra.matrices._
import net.tqft.toolkit.permutations.Permutation
import net.tqft.toolkit.permutations.Permutations
import net.tqft.toolkit.algebra.polynomials.Polynomial

trait FiniteDimensionalFreeModule[A] extends Module[A, Seq[A]] {
  def coefficients: Ring[A]
  def rank: Int
  override lazy val zero = Seq.fill(rank)(coefficients.zero)
  override def add(x: Seq[A], y: Seq[A]) = x.zip(y).map(p => coefficients.add(p._1, p._2))
  override def scalarMultiply(a: A, b: Seq[A]) = b.map(x => coefficients.multiply(a, x))
  override def negate(x: Seq[A]) = x.map(coefficients.negate)
  
  def innerProduct(x: Seq[A], y: Seq[A]) = coefficients.add(x.zip(y).map(p => coefficients.multiply(p._1, p._2)))
  
  def basis = for (i <- 0 until rank) yield for (j <- 0 until rank) yield if (i == j) coefficients.one else coefficients.zero
}

// Usually A = Int, for a concrete fusion ring. We allow other possibilities so we can write fusion solvers, etc.
trait FusionRing[A] extends FiniteDimensionalFreeModule[A] with Rig[Seq[A]] { fr =>

  override def fromInt(x: Int) = coefficients.fromInt(x) +: Seq.fill(rank - 1)(coefficients.zero)
  override val one = fromInt(1)

  def associativityConstraints = for (x <- basis.iterator; y <- basis; z <- basis) yield subtract(multiply(x, multiply(y, z)), multiply(multiply(x, y), z))
  def identityConstraints = (for (x <- basis.iterator) yield Seq(subtract(x, multiply(one, x)), subtract(x, multiply(x, one)))).flatten
  def dualityConstraints(duality: Permutation = duality) = for (x <- basis.iterator; y <- basis) yield {
    import net.tqft.toolkit.permutations.Permutations._
    subtract(duality.permute(multiply(x, y)), multiply(duality.permute(y), duality.permute(x)))
  }

  def duality: Permutation = {
    structureCoefficients.map(_.entries.indexWhere(_.head == coefficients.one).ensuring(_ != -1))
  }

  def verifyAssociativity = associativityConstraints.map(_ == zero).reduce(_ && _)
  def verifyIdentity = identityConstraints.map(_ == zero).reduce(_ && _)
  def verifyDuality(duality: Permutation = duality) = dualityConstraints(duality).map(_ == zero).reduce(_ && _)

  lazy val structureCoefficients = for (y <- basis) yield Matrix(rank, for (x <- basis) yield multiply(x, y))

  trait FusionModule extends FiniteDimensionalFreeModule[A] { fm =>
    override def coefficients = fr.coefficients

    def fusionRing = fr

    def act(x: Seq[A], m: Seq[A]): Seq[A]
    def rightMultiplicationByDuals(m: Seq[A], n: Seq[A]): Seq[A] = for(i <- 0 until fr.rank) yield {
      innerProduct(m, act(fr.basis(i), n))
    }
    
    def associativityConstraints = for (x <- fr.basis.iterator; y <- fr.basis; z <- basis) yield subtract(act(x, act(y, z)), act(fr.multiply(x, y), z))
    def admissibilityConstraints = for(m <- basis.iterator; x <- fr.basis; h <- fr.basis) yield {
      coefficients.subtract(innerProduct(act(x, m), act(h, m)), fr.innerProduct(fr.multiply(x, rightMultiplicationByDuals(m, m)), h))
    }
    def identityConstraints = for (x <- basis.iterator) yield subtract(x, act(fr.one, x))
    
    def verifyAssociativity = associativityConstraints.map(_ == zero).reduce(_ && _)
    def verifyAdmissibility = admissibilityConstraints.map(_ == coefficients.zero).reduce(_ && _)
    
    def asMatrix(x: Seq[A]) = new Matrix(rank, for (b <- basis) yield act(x, b))

    def structureCoefficients = for (y <- basis) yield Matrix(rank, for (x <- fr.basis) yield act(x, y))

    override def equals(other: Any) = {
      other match {
        case other: fr.FusionModule => structureCoefficients == other.structureCoefficients
        case _ => false
      }
    }
    override def hashCode = (fr, structureCoefficients).hashCode

  }

  object FusionModules {
    def equivalent_?(m1: FusionModule, m2: FusionModule) = {
      if (m1.rank == m2.rank) {
        import net.tqft.toolkit.permutations.Permutations
        import net.tqft.toolkit.permutations.Permutations._

        val s1 = m1.structureCoefficients
        val s2 = m2.structureCoefficients

        Permutations.of(m1.rank).exists(p => s2 == p.permute(s1.map(m => m.permuteColumns(p))))
      } else {
        false
      }
    }
  }

  protected class StructureCoefficientFusionModule(matrices: Seq[Matrix[A]]) extends FusionModule {
    for (m <- matrices) {
      require(m.numberOfColumns == matrices.size)
      require(m.numberOfRows == fr.rank)
    }
    override val rank = matrices.size
    override def act(x: Seq[A], m: Seq[A]) = {
      require(m.size == rank)
      val zero = coefficients.zero
      val terms = for (
        (xi, i) <- x.zipWithIndex;
        if (xi != zero);
        (mj, j) <- m.zipWithIndex;
        if (mj != zero)
      ) yield {
        for (k <- 0 until rank) yield coefficients.multiply(xi, mj, matrices(j).entries(i)(k))
      }
      val result = add(terms)
      require(m.size == result.size)
      result
    }
  }

  def moduleFromStructureCoefficients(matrices: Seq[Matrix[A]]): FusionModule = {
    (new StructureCoefficientFusionModule(matrices)) //.ensuring(_.structureCoefficients == matrices)
  }

  trait RegularModule extends FusionModule {
    override val rank = fr.rank
    override def act(x: Seq[A], m: Seq[A]) = fr.multiply(x, m)
  }

  lazy val regularModule: RegularModule = new RegularModule {}
}

trait ConcreteFusionRing extends FusionRing[Int] {
  def dimensionLowerBounds(x: Seq[Int]): Double = {
    val matrices = Matrices.over[Int]
    val A = x.zip(structureCoefficients).map(p => matrices.scalarMultiply(p._1, p._2)).reduce(matrices.add)
    val AAt = matrices.compose(A, A.transpose)
    val result = scala.math.sqrt(FrobeniusPerronEigenvalues.estimate(AAt) - 0.0001)
    result
  }

  trait FusionModule extends super.FusionModule {
    def dimensionLowerBounds(x: Seq[Int]): Double = {
      if (x.forall(_ == 0)) {
        0
      } else {
        val matrices = Matrices.over[Int]
        val A = x.zip(structureCoefficients).map(p => matrices.scalarMultiply(p._1, p._2)).reduce(matrices.add)
        require(A.entries.flatten.forall(_ >= 0))
        require(A.entries.flatten.exists(_ > 0))
        val AAt = matrices.compose(A, A.transpose)
        val estimate = FrobeniusPerronEigenvalues.estimate(AAt)
        require(estimate > 0.999)
        val result = scala.math.sqrt(estimate - 0.0001)
        result
      }
    }
  }

  override def moduleFromStructureCoefficients(matrices: Seq[Matrix[Int]]): FusionModule = {
    (new StructureCoefficientFusionModule(matrices)) // .ensuring(_.structureCoefficients == matrices)
  }

  protected class StructureCoefficientFusionModule(matrices: Seq[Matrix[Int]]) extends super.StructureCoefficientFusionModule(matrices) with FusionModule

}

object FusionRing {
  def apply[A: Ring](multiplicities: Seq[Matrix[A]]): FusionRing[A] = {
    val result = new StructureCoefficientFusionRing(multiplicities)
    //    require({
    //      val sc = result.structureCoefficients
    //      sc == multiplicities
    //    })
    result
  }
  def apply(multiplicities: Seq[Matrix[Int]]): ConcreteFusionRing = {
    new ConcreteStructureCoefficientFusionRing(multiplicities)
  }
  def apply(multiplicities: Seq[Matrix[Int]], fieldGenerator: Polynomial[Int], fieldGeneratorApproximation: Double, fieldGeneratorEpsilon: Double, dimensions: Seq[Polynomial[Fraction[Int]]]): FusionRingWithDimensions = new StructureCoefficientFusionRingWithDimensions(multiplicities, fieldGenerator, fieldGeneratorApproximation, fieldGeneratorEpsilon, dimensions).ensuring(_.structureCoefficients == multiplicities)

  private class StructureCoefficientFusionRing[A: Ring](multiplicities: Seq[Matrix[A]]) extends FusionRing[A] {
    override lazy val coefficients = implicitly[Ring[A]]
    override lazy val rank = multiplicities.size

    override def multiply(x: Seq[A], y: Seq[A]) = {
      val zero = coefficients.zero
      val terms = for (
        (xi, i) <- x.zipWithIndex;
        if (xi != zero);
        (yj, j) <- y.zipWithIndex;
        if (yj != zero)
      ) yield {
        for (k <- 0 until rank) yield {
          coefficients.multiply(xi, yj, multiplicities(j).entries(i)(k))
        }
      }
      val result = add(terms)
      result
    }
  }

  private class ConcreteStructureCoefficientFusionRing(multiplicities: Seq[Matrix[Int]]) extends StructureCoefficientFusionRing[Int](multiplicities) with ConcreteFusionRing

  private class StructureCoefficientFusionRingWithDimensions(multiplicities: Seq[Matrix[Int]], fieldGenerator: Polynomial[Int], fieldGeneratorApproximation: Double, fieldGeneratorEpsilon: Double, override val dimensions: Seq[Polynomial[Fraction[Int]]]) extends StructureCoefficientFusionRing[Int](multiplicities)(Gadgets.Integers) with FusionRingWithDimensions {
    override def dimensionField = {
      RealNumberField(fieldGenerator, fieldGeneratorApproximation, fieldGeneratorEpsilon)(Gadgets.Integers, Gadgets.Doubles)
    }
  }
}

object Goals extends App {

  val H1 = FusionRings.Examples.H1
  val AH1 = FusionRings.Examples.AH1

  H1.verifyDuality()
  H1.verifyDuality(IndexedSeq(0, 1, 2, 3))


  val H1r = H1.regularModule.ensuring(_.verifyAdmissibility)
  val bm = FusionBimoduleWithLeftDimensions(H1.regularModule, H1.structureCoefficients, H1r.structureCoefficients).ensuring(_.verifyAssociativity).ensuring(_.verifyAdmissibility).ensuring(_.verifyIdentity)

  for(fm <- H1.candidateFusionModules) {
    println(fm.verifyAdmissibility)
    println(H1.FusionModules.equivalent_?(fm, H1r))
  }
//  println(FusionBimodules.commutants(H1r, rankBound=Some(6)).size)
  
  for (fm <- H1.candidateFusionModules) {
    val commutants = FusionBimodules.commutants(fm, rankBound = Some(6))
    println(fm + " has " + commutants.size + " commutants")
  }

  //  for (fm <- H1.candidateFusionModules; b <- FusionBimodules.commutants(fm, 4, 4, None)) {
  //    println(b.rightRing.structureCoefficients)
  //  }

  //    for (r <- FusionRings.withObject(AH1.structureCoefficients(1)); m <- r.structureCoefficients) { println(m); println() }

  def test(G: FusionRingWithDimensions) {
    println("Start: " + new java.util.Date())
    println("dimension bounds: " + G.basis.map(G.dimensionUpperBounds))
    println(G.candidateAlgebraObjects.toList)
    for (m <- G.candidateFusionMatrices) {
      println(m.algebraObject)
      println(m.matrix)
      println(m.dimensionsSquared)
      println(m.dimensionsSquared.map(G.dimensionField.approximateWithin(0.001)))
    }
    println("Finish: " + new java.util.Date())
    //    println(G.candidateFusionModules.size)
    var count = 0
    for (fm <- G.candidateFusionModules) {
      println(fm.structureCoefficients)
      count = count + 1
      println("found " + count + " modules so far")
    }
  }
  //  test(H1)
  //  test(AH1)

  //  val v = Seq(1, 1, 2, 3, 3, 4)
  ////  val v = Seq(1,2,3,4,5,7)
  //  println(AH1.objectsSmallEnoughToBeAlgebras.contains(v))
  //  println(AH1.smallObjectsWithPositiveSemidefiniteMultiplication.contains(v))
  //  println(AH1.regularModule.asMatrix(v));
  //  {
  //    import Implicits.Rationals
  //    println(AH1.regularModule.asMatrix(v).mapEntries(Implicits.integersAsRationals).positiveSemidefinite_?)
  //  }
  //  println(Matrices.positiveSymmetricDecompositions(AH1.regularModule.asMatrix(v)).toList)
  //  haagerupFusionRing.candidateBrauerPicardGroupoids
}