package net.tqft.toolkit.algebra.fusion

import net.tqft.toolkit.algebra._
import net.tqft.toolkit.algebra.diophantine._
import net.tqft.toolkit.algebra.matrices._
import net.tqft.toolkit.algebra.polynomials._

object FusionBimodules extends net.tqft.toolkit.Logging {
  def commutants(leftModule: FusionRingWithDimensions#FusionModule, rankBound: Option[Int] = None): Iterable[FusionBimodule[Int]] = {
    val upperBoundFromGlobalDimension = leftModule.fusionRing.globalDimensionUpperBound.floor.toInt
    val upperBoundFromMatrixBlocks = 1 + implicitly[Ring[Int]].power(leftModule.fusionRing.rank - 1, 2) // this is probably just bogus
    val upperBound = (rankBound ++ List(upperBoundFromGlobalDimension, upperBoundFromMatrixBlocks)).min

    (for (otherRank <- (upperBound to 1 by -1).par; commutant <- commutantsWithRank(leftModule, otherRank)) yield commutant).seq
  }

  def commutantsWithRank(leftModule: FusionRingWithDimensions#FusionModule, otherRank: Int): Iterable[FusionBimodule[Int]] = {
    (for (nsd <- (otherRank to 0 by -2).par; commutant <- commutantsWithRank(leftModule, otherRank, nsd)) yield commutant).seq
  }

  def commutantsWithRank(leftModule: FusionRingWithDimensions#FusionModule, otherRank: Int, otherNumberOfSelfDualObjects: Int, knownBimodule: Option[FusionBimodule[Int]] = None): Iterable[FusionBimodule[Int]] = {

    info("Trying to find commutants with total rank " + otherRank + " and " + otherNumberOfSelfDualObjects + " self-dual objects.")

    // first, make a FusionBimodule with variable entries

    val leftRing = leftModule.fusionRing
    type V = (Int, Int, Int, Int)

    implicit val polynomialAlgebra = MultivariablePolynomialAlgebras.over[Int, V]

    val fusionRingUnknowns = for (i <- 0 until otherRank) yield {
      Matrix(otherRank,
        for (j <- 0 until otherRank) yield {
          for (k <- 0 until otherRank) yield polynomialAlgebra.monomial((0, i, j, k))
        })
    }
    val fusionModuleUnknowns = for (i <- 0 until leftModule.rank) yield {
      Matrix(leftModule.rank,
        for (j <- 0 until otherRank) yield {
          for (k <- 0 until leftModule.rank) yield polynomialAlgebra.monomial((1, i, j, k))
        })

    }

    val leftRingStructureCoefficients = leftRing.structureCoefficients.map(_.mapEntries(x => polynomialAlgebra.constant(x)))
    val leftModuleStructureCoefficients = leftModule.structureCoefficients.map(_.mapEntries(x => polynomialAlgebra.constant(x)))

    val variableBimodule = FusionBimodule(leftRingStructureCoefficients, leftModuleStructureCoefficients, fusionRingUnknowns, fusionModuleUnknowns)

    val otherDuality = (0 until otherNumberOfSelfDualObjects) ++ (for (i <- otherNumberOfSelfDualObjects until otherRank by 2; j <- Seq(i + 1, i)) yield j)
    require(otherDuality.size == otherRank)

    val knownSolution = knownBimodule.map({ bimodule =>
      val rrsc = bimodule.rightRing.structureCoefficients
      val rmsc = bimodule.rightModule.structureCoefficients
      ((for (i <- 0 until otherRank; j <- 0 until otherRank; k <- 0 until otherRank) yield (0, i, j, k) -> rrsc(i).entries(j)(k)) ++
        (for (i <- 0 until leftModule.rank; j <- 0 until otherRank; k <- 0 until leftModule.rank) yield (1, i, j, k) -> rmsc(i).entries(j)(k))).toMap
    })

    def reconstituteBimodule(m: Map[V, Int]): FusionBimoduleWithLeftDimensions = {
      val rightRingStructureCoefficients = fusionRingUnknowns.map(_.mapEntries({
        case p if p.totalDegree == Some(1) => m(p.terms.head._1.keySet.iterator.next)
        case p => p.constantTerm
      }))
      val rightModuleStructureCoefficients = fusionModuleUnknowns.map(_.mapEntries({
        case p if p.totalDegree == Some(1) => m(p.terms.head._1.keySet.iterator.next)
        case p => p.constantTerm
      }))
      FusionBimoduleWithLeftDimensions(leftModule, rightRingStructureCoefficients, rightModuleStructureCoefficients)
    }

    def checkInequalities(m: Map[V, Int]): Boolean = {
      val bimodule = reconstituteBimodule(m)
      //     println(bimodule.rightModule.structureCoefficients)
      //     println(bimodule.rightRing.structureCoefficients)
      bimodule.verifyRightSmallerThanLeftInequalities && bimodule.verifyGlobalDimensionInequality
    }

    val polynomials = (variableBimodule.associativityConstraints ++ variableBimodule.admissibilityConstraints ++ variableBimodule.identityConstraints ++ variableBimodule.rightRing.dualityConstraints(otherDuality)).map(p => polynomialAlgebra.subtract(p._1, p._2)).toSeq
    val variables = (fusionModuleUnknowns.flatMap(_.entries).flatten.flatMap(_.variables.toSeq) ++ fusionRingUnknowns.flatMap(_.entries).flatten.flatMap(_.variables.toSeq)).distinct
    require(variables.size == otherRank * otherRank * otherRank + leftModule.rank * otherRank * leftModule.rank)

    val (solutions, tooHard) = BoundedDiophantineSolver.solve(
      polynomials, variables, boundary = Some(checkInequalities _), knownSolution = knownSolution)

    require(tooHard.isEmpty)

    info("... finished finding commutants with total rank " + otherRank + " and " + otherNumberOfSelfDualObjects + " self-dual objects.")

    def equivalent_?(b1: FusionBimoduleWithLeftDimensions, b2: FusionBimoduleWithLeftDimensions): Boolean = {
      import net.tqft.toolkit.permutations.Permutations
      import net.tqft.toolkit.permutations.Permutations._

      val r1 = b1.rightRing.structureCoefficients
      val r2 = b2.rightRing.structureCoefficients
      val m1 = b1.rightModule.structureCoefficients
      val m2 = b2.rightModule.structureCoefficients

      Permutations.of(r1.size).exists({ p =>
        r2 == p.permute(r1.map(m => m.permuteColumns(p).permuteRows(p))) &&
          m2 == m1.map(m => m.permuteRows(p))
      })
    }

    import net.tqft.toolkit.collections.GroupBy._

    (for (solution <- solutions) yield reconstituteBimodule(solution)).chooseEquivalenceClassRepresentatives(equivalent_?)
  }

  def withGenerator(leftGenerator: Matrix[Int], rightGenerator: Matrix[Int], leftDuality: IndexedSeq[Int], rightDuality: IndexedSeq[Int]): Iterable[FusionBimodule[Int]] = {
    val leftRank = leftGenerator.numberOfRows
    val rightRank = rightGenerator.numberOfRows
    val moduleRank = leftGenerator.numberOfColumns.ensuring(_ == rightGenerator.numberOfColumns)

    type V = (Int, Int, Int, Int)

    implicit val polynomialAlgebra = MultivariablePolynomialAlgebras.over[Int, V]

    val leftMultiplication = for (i <- 0 until leftRank) yield {
      Matrix(leftRank,
        for (j <- 0 until leftRank) yield {
          for (k <- 0 until leftRank) yield polynomialAlgebra.monomial((0, i, j, k))
        })
    }
    val leftAction = leftGenerator.mapEntries(polynomialAlgebra.constant) +: (for (i <- 1 until moduleRank) yield {
      Matrix(moduleRank,
        for (j <- 0 until leftRank) yield {
          for (k <- 0 until moduleRank) yield polynomialAlgebra.monomial((1, i, j, k))
        })
    })
    val rightMultiplication = for (i <- 0 until rightRank) yield {
      Matrix(rightRank,
        for (j <- 0 until rightRank) yield {
          for (k <- 0 until rightRank) yield polynomialAlgebra.monomial((2, i, j, k))
        })
    }
    val rightAction = rightGenerator.mapEntries(polynomialAlgebra.constant) +: (for (i <- 1 until moduleRank) yield {
      Matrix(moduleRank,
        for (j <- 0 until rightRank) yield {
          for (k <- 0 until moduleRank) yield polynomialAlgebra.monomial((3, i, j, k))
        })
    })

    val variableBimodule = FusionBimodule(leftMultiplication, leftAction, rightMultiplication, rightAction)

    val polynomials = (variableBimodule.associativityConstraints ++ variableBimodule.admissibilityConstraints ++ variableBimodule.identityConstraints ++ variableBimodule.leftRing.dualityConstraints(leftDuality) ++ variableBimodule.rightRing.dualityConstraints(rightDuality)).map(p => polynomialAlgebra.subtract(p._1, p._2)).toSeq
    val variables = (leftMultiplication.flatMap(_.entries).flatten.flatMap(_.variables.toSeq) ++
      leftAction.tail.flatMap(_.entries).flatten.flatMap(_.variables.toSeq) ++
      rightMultiplication.flatMap(_.entries).flatten.flatMap(_.variables.toSeq) ++
      rightAction.tail.flatMap(_.entries).flatten.flatMap(_.variables.toSeq))

    def reconstituteBimodule(m: Map[V, Int]): FusionBimodule[Int] = {
      def substitute(matrices: Seq[Matrix[MultivariablePolynomial[Int, V]]]): Seq[Matrix[Int]] = {
        matrices.map(_.mapEntries({
          case p if p.totalDegree == Some(1) => m(p.terms.head._1.keySet.iterator.next)
          case p => p.constantTerm
        }))
      }

      FusionBimodule(substitute(leftMultiplication), substitute(leftAction), substitute(rightMultiplication), substitute(rightAction))
    }

    val (solutions, tooHard) = BoundedDiophantineSolver.solve(polynomials, variables)
    require(tooHard.isEmpty)
    for(s <- solutions) yield reconstituteBimodule(s)
  }

}