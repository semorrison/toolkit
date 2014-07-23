package net.tqft.toolkit.algebra.spiders.examples

import net.tqft.toolkit.algebra._
import net.tqft.toolkit.algebra.spiders._
import net.tqft.toolkit.algebra.mathematica._
import net.tqft.toolkit.algebra.polynomials._
import net.tqft.toolkit.collections.KSubsets
import net.tqft.toolkit.algebra.matrices.Matrix
import MathematicaForm._

case class SpiderData(
  spider: QuotientSpider,
  groebnerBasis: Seq[MultivariablePolynomial[Fraction[BigInt], String]],
  relations: Seq[Seq[Map[PlanarGraph, MultivariablePolynomial[Fraction[BigInt], String]]]],
  dimensionBounds: Seq[Int],
  consideredDiagramsVertexBound: Seq[Int],
  consideredDiagrams: Seq[Seq[PlanarGraph]],
  independentDiagrams: Seq[Seq[PlanarGraph]],
  visiblyIndependentDiagrams: Seq[Seq[PlanarGraph]]) {

  // verify
  def verify {
    for ((n, (k, j)) <- dimensionBounds.zip(independentDiagrams.map(_.size).zip(visiblyIndependentDiagrams.map(_.size)))) {
      require(k <= n)
      require(j >= 2 * k - n)
    }

    for (visiblyIndependent <- visiblyIndependentDiagrams) {
      // the determinants of visibly independent vectors must be invertible
      val det = calculateDeterminant(visiblyIndependent)
      require(declarePolynomialZero(det).isEmpty)
    }
  }

  type P = MultivariablePolynomial[Fraction[BigInt], String]

  override def toString = {
    s"""SpiderData(
  groebnerBasis = ${groebnerBasis.toMathematicaInputString},
  dimensionBounds = $dimensionBounds,
  independentDiagrams.sizes: ${independentDiagrams.map(_.size)},
  visiblyIndependentDiagrams.sizes: ${visiblyIndependentDiagrams.map(_.size)},
  consideredDiagrams.sizes: ${consideredDiagrams.map(_.size)},
  allPolyhedra: $allPolyhedra,
  reducibleDiagrams: ${spider.extraReductions.map(_.big)},
  consideredDiagramsVertexBound = $consideredDiagramsVertexBound
)"""
  }

  def allPolyhedra =
    (groebnerBasis.flatMap(_.variables) ++
      spider.reductions.flatMap(_.small.values.flatMap(r => r.variables)) ++
      relations.flatMap(_.flatMap(_.values.flatMap(_.variables)))).distinct

  val complexity: Ordering[PlanarGraph] = {
    Ordering.by(_.numberOfInternalVertices)
  }

  // This forces all the formal inverses to come last in the variable ordering, which seems to help the Groebner bases.
  implicit object stringOrdering extends Ordering[String] {
    override def compare(x: String, y: String) = {
      if (x.endsWith("^(-1)") && !y.endsWith("^(-1)")) {
        1
      } else if (!x.endsWith("^(-1)") && y.endsWith("^(-1)")) {
        -1
      } else {
        Ordering.String.compare(x, y)
      }
    }
  }

  lazy val polynomials = MultivariablePolynomialAlgebras.quotient(groebnerBasis)
  lazy val rationalFunctions = Field.fieldOfFractions(polynomials)

  def addReduction(r: Reduction[PlanarGraph, MultivariablePolynomial[Fraction[BigInt], String]]): Seq[SpiderData] = {
    val newSpider = spider.addReduction(r)

    def reevaluateAllPolyhedra: Seq[SpiderData] = {
      val differences = (for (p <- allPolyhedra.iterator; g <- PolyhedronNamer.byName(p).iterator; r <- newSpider.allEvaluations(g)) yield {
        rationalFunctions.subtract(Fraction.whole(polynomials.monomial(p)), r).numerator
      }).toSeq.distinct

      println("reevaluateAllPolyhedra found differences: " + differences.toMathematicaInputString)

      differences.foldLeft(Seq(this))({ (s, d) => s.flatMap(r => r.declarePolynomialZero(d)) })
    }
    reevaluateAllPolyhedra.map(_.copy(spider = newSpider).simplifyReductions)
  }

  def simplifyReductions: SpiderData = {
    spider.extraReductions.tails.find(tail => tail.nonEmpty && tail.tail.find(d => tail.head.big.Subgraphs(d.big).excisions.nonEmpty).nonEmpty) match {
      case Some(tail) => copy(spider = spider.copy(extraReductions = spider.extraReductions.filterNot(_.big == tail.head.big))).simplifyReductions
      case None => this
    }
  }

  def innerProducts(diagrams1: Seq[PlanarGraph], diagrams2: Seq[PlanarGraph]): Seq[Seq[MultivariablePolynomial[Fraction[BigInt], String]]] = {
    spider.innerProductMatrix(diagrams1, diagrams2).map(row => row.map(entry => polynomials.normalForm(entry)))
  }

  def innerProducts(diagrams: Seq[PlanarGraph]): Seq[Seq[MultivariablePolynomial[Fraction[BigInt], String]]] = innerProducts(diagrams, diagrams)

  def calculateDeterminant(diagrams: Seq[PlanarGraph]) = {
    val matrix = innerProducts(diagrams)
    println("computing determinant of: ")
    println(matrix.toMathematicaInputString)

    import mathematica.Determinant.ofMultivariablePolynomialMatrix._
    val result = polynomials.normalForm(matrix.determinant)

    println("determinant: ")
    println(result.toMathematicaInputString)

    result
  }

  def considerDiagram(p: PlanarGraph): Seq[SpiderData] = {
    println("considering diagram: " + p)
    println("for: " + this)

    val boundary = p.numberOfBoundaryPoints
    val newConsideredDiagrams = {
      val padded = consideredDiagrams.padTo(boundary + 1, Seq.empty)
      padded.updated(boundary, (padded(boundary) :+ p).distinct)
    }
    val paddedIndependentDiagrams = independentDiagrams.padTo(boundary + 1, Seq.empty)
    val paddedVisiblyIndependentDiagrams = visiblyIndependentDiagrams.padTo(boundary + 1, Seq.empty)
    val padded = copy(independentDiagrams = paddedIndependentDiagrams, visiblyIndependentDiagrams = paddedVisiblyIndependentDiagrams)

    val addDependent = padded.addDependentDiagram(p)
    val addIndependent = if (padded.independentDiagrams(boundary).size < dimensionBounds(boundary)) {
      padded.addIndependentDiagram(p)
    } else {
      Seq.empty
    }

    (addDependent ++ addIndependent).map(_.copy(consideredDiagrams = newConsideredDiagrams))
  }

  def addDependentDiagram(p: PlanarGraph): Seq[SpiderData] = {
    println("adding a dependent diagram: " + p)

    val rectangularMatrix = innerProducts(visiblyIndependentDiagrams(p.numberOfBoundaryPoints), independentDiagrams(p.numberOfBoundaryPoints) :+ p).map(row => row.map(entry => Fraction.whole(entry)))

    println("computing nullspace of: ")
    println(rectangularMatrix.toMathematicaInputString)
    import mathematica.NullSpace.ofMultivariableRationalFunctionMatrix._

    val nullSpace = {
      if (independentDiagrams(p.numberOfBoundaryPoints).size == 0) {
        Seq(Seq(rationalFunctions.one))
      } else {
        rectangularMatrix.nullSpace
      }
    }

    println("nullspace: ")
    println(nullSpace.toMathematicaInputString)

    val relation = nullSpace.ensuring(_.size == independentDiagrams(p.numberOfBoundaryPoints).size - visiblyIndependentDiagrams(p.numberOfBoundaryPoints).size + 1).head
    require(relation.last == rationalFunctions.one)

    // is it a reducing relation?
    val nonzeroPositions = relation.dropRight(1).zipWithIndex.collect({ case (x, i) if !rationalFunctions.zero_?(x) => i })
    val reducing = relation.size > 1 && nonzeroPositions.forall({ i => complexity.lt(independentDiagrams(p.numberOfBoundaryPoints)(i), p) })
    println(s"reducing: $reducing")

    if (reducing && p.vertexFlags.head.map(_._1).distinct.size == p.numberOfBoundaryPoints /* FIXME a very annoying implementation restriction */ ) {
      // are there denominators? we better ensure they are invertible
      val denominatorLCM = polynomials.lcm(relation.map(_.denominator): _*)

      val whenDenominatorsVanish = declarePolynomialZero(denominatorLCM).toSeq.flatMap(_.addDependentDiagram(p))

      require(p.numberOfInternalVertices > 0)
      val newReduction = Reduction(p, independentDiagrams(p.numberOfBoundaryPoints).zip(relation.dropRight(1).map(rationalFunctions.negate).map(liftDenominators)).toMap)
      val whenDenominatorsNonzero = invertPolynomial(denominatorLCM).map(_._1).toSeq.flatMap(_.addReduction(newReduction))

      whenDenominatorsVanish ++ whenDenominatorsNonzero
    } else {
      // TODO record non-reducing relations!

      // There's a lemma here. If b \in span{a1, ..., an}, and {a1, ..., ak} is maximally visibly independent,
      // then the inner product determinant for {a1, ..., ak, b} vanishes.
      declarePolynomialZero(calculateDeterminant(visiblyIndependentDiagrams(p.numberOfBoundaryPoints) :+ p))
    }

  }

  def addIndependentDiagram(p: PlanarGraph): Seq[SpiderData] = {
    println(" adding an independent diagram: " + p)

    val newIndependentDiagrams = independentDiagrams.updated(p.numberOfBoundaryPoints, independentDiagrams(p.numberOfBoundaryPoints) :+ p)

    val addVisibly = addVisiblyIndependentDiagram(p).filter({ s =>
      s.visiblyIndependentDiagrams(p.numberOfBoundaryPoints).size >= 2 * independentDiagrams(p.numberOfBoundaryPoints).size + 2 - dimensionBounds(p.numberOfBoundaryPoints)
    })
    val addInvisibly = if (visiblyIndependentDiagrams(p.numberOfBoundaryPoints).size >= 2 * independentDiagrams(p.numberOfBoundaryPoints).size + 2 - dimensionBounds(p.numberOfBoundaryPoints)) {
      addInvisiblyIndependentDiagram(p)
    } else {
      Seq.empty
    }

    (addVisibly ++ addInvisibly).map(_.copy(independentDiagrams = newIndependentDiagrams))
  }

  def addVisiblyIndependentDiagram(p: PlanarGraph): Seq[SpiderData] = {
    println("adding a visibly independent diagram: " + p)

    val invisibleDiagrams = independentDiagrams(p.numberOfBoundaryPoints).filterNot(visiblyIndependentDiagrams(p.numberOfBoundaryPoints).contains)
    println("there are currently " + invisibleDiagrams.size + " invisible diagrams")
    val invisibleSubsets = {
      import net.tqft.toolkit.collections.Subsets._
      invisibleDiagrams.subsets.map(_.toSeq).toSeq.ensuring(s => s.size == 1 << invisibleDiagrams.size)
    }
    println("there are " + invisibleSubsets.size + " subsets of formerly invisible diagrams, which we need to reconsider.")
    val determinants = {
      (for (s <- invisibleSubsets) yield {
        println("considering a subset of size " + s.size)
        s -> calculateDeterminant(visiblyIndependentDiagrams(p.numberOfBoundaryPoints) ++ s :+ p)
      }).toMap
    }

    val determinantsWithBiggerDeterminants = {
      (for (s <- invisibleSubsets) yield {
        s -> (determinants(s), invisibleSubsets.filter(t => t.size > s.size && s.forall(t.contains)).map(determinants))
      })
    }

    (for ((s, (nonzero, allZero)) <- determinantsWithBiggerDeterminants) yield {
      val newVisiblyIndependentDiagrams = visiblyIndependentDiagrams.updated(p.numberOfBoundaryPoints, visiblyIndependentDiagrams(p.numberOfBoundaryPoints) ++ s :+ p)

      invertPolynomial(nonzero).map(_._1).toSeq.flatMap(_.declareAllPolynomialsZero(allZero)).map(_.copy(visiblyIndependentDiagrams = newVisiblyIndependentDiagrams))
    }).flatten
  }

  def addInvisiblyIndependentDiagram(p: PlanarGraph): Seq[SpiderData] = {
    println("adding an invisibly independent diagram: " + p)

    val invisibleDiagrams = independentDiagrams(p.numberOfBoundaryPoints).filterNot(visiblyIndependentDiagrams(p.numberOfBoundaryPoints).contains)
    val invisibleSubsets = {
      import net.tqft.toolkit.collections.Subsets._
      invisibleDiagrams.subsets.map(_.toSeq).toSeq
    }
    val determinants = {
      for (s <- invisibleSubsets) yield {
        calculateDeterminant(independentDiagrams(p.numberOfBoundaryPoints) ++ s :+ p)
      }
    }

    declareAllPolynomialsZero(determinants)
  }

  def normalizePolynomial(p: P) = {
    def bigRationals = implicitly[Field[Fraction[BigInt]]]
    polynomials.leadingMonomial(p) match {
      case Some(lm) => polynomials.scalarMultiply(bigRationals.inverse(p.coefficients(lm)), p)
      case None => polynomials.zero
    }

  }

  def liftDenominators(p: MultivariableRationalFunction[Fraction[BigInt], String]): P = polynomials.multiply(p.numerator, invertPolynomial(p.denominator).get._2)

  def invertPolynomial(p: P): Option[(SpiderData, P)] = {
    import mathematica.Factor._
    println("Factoring something we're inverting: " + p.toMathematicaInputString)
    val factors = p.factor
    println("Factors: ")
    for (f <- factors) println(f._1.toMathematicaInputString + "   ^" + f._2)

    val result = factors.foldLeft[Option[(SpiderData, P)]](Some(this, polynomials.one))({ (s: Option[(SpiderData, P)], q: (P, Int)) =>
      s.flatMap({
        z: (SpiderData, P) =>
          if (q._2 > 0) {
            z._1.invertIrreduciblePolynomial(q._1).map({ w =>
              (w._1, polynomials.multiply(polynomials.power(w._2, q._2), z._2))
            })
          } else {
            Some((z._1, polynomials.multiply(polynomials.power(q._1, -q._2), z._2)))
          }
      })
    })

    if (polynomials.totalDegree(p) > 0) {
      require(result.isEmpty || result.get._1.groebnerBasis.nonEmpty)
    }

    result
  }
  def inverse(p: P): P = {
    if (polynomials.totalDegree(p) == 0) {
      polynomials.ring.inverse(polynomials.constantTerm(p))
    } else {
      p.toMathematicaInputString match {
        case s if s.startsWith("(") && s.endsWith("^(-1)") => polynomials.monomial(s.stripPrefix("(").stripSuffix("^(-1)"))
        case s if s.contains("^(-1)") => ???
        case s => polynomials.monomial("(" + s + ")^(-1)")
      }
    }
  }
  def invertIrreduciblePolynomial(p: P): Option[(SpiderData, P)] = {
    println("inverting " + p.toMathematicaInputString)
    if (polynomials.zero_?(p)) {
      None
    } else {
      val i = inverse(p)
      declareIrreduciblePolynomialZero(polynomials.subtract(polynomials.multiply(i, p), polynomials.one)).map(s => (s, i))
    }
  }

  def declareAtLeastOnePolynomialZero(rs: Seq[P]): Seq[SpiderData] = {
    rs.flatMap(declarePolynomialZero)
  }

  def declareAllPolynomialsZero(rs: Seq[P]): Seq[SpiderData] = {
    rs.foldLeft(Seq(this))({ (data: Seq[SpiderData], r: P) => data.flatMap(_.declarePolynomialZero(r)) })
  }

  def declarePolynomialZero(r: P): Seq[SpiderData] = {
    if (polynomials.zero_?(r)) {
      Seq(this)
    } else if (r == polynomials.one || r == polynomials.negativeOne) {
      Seq.empty
    } else {
      val factors = {
        import mathematica.Factor._
        println("Factoring something we're going to set to zero: " + r.toMathematicaInputString)
        normalizePolynomial(r).factor.filter(_._2 > 0).keys.toSeq.ensuring(_.nonEmpty)
      }
      factors.flatMap(f => { require(!f.toMathematicaInputString.contains("^(-1)")); declareIrreduciblePolynomialZero(f) })
    }
  }

  def declareIrreduciblePolynomialZero(r: P): Option[SpiderData] = {

    val newGroebnerBasis = {
      import mathematica.GroebnerBasis._
      println("Computing Groebner basis for " + (groebnerBasis :+ r).toMathematicaInputString)

      val result = (groebnerBasis :+ r).computeGroebnerBasis
      println(" ... result: " + result.toMathematicaInputString)
      result
    }

    // has everything collapsed?
    if (newGroebnerBasis.contains(polynomials.one)) {
      None
    } else {
      val newPolynomials = MultivariablePolynomialAlgebras.quotient(newGroebnerBasis)
      // TODO if we have non-reducing relations, these will have to be updated as well
      val newExtraReductions = spider.extraReductions.map({
        case Reduction(big, small) => Reduction(big, small.map({ p => (p._1, newPolynomials.normalForm(p._2)) }))
      })
      Some(copy(
        spider = spider.copy(extraReductions = newExtraReductions),
        groebnerBasis = newGroebnerBasis))
    }

  }

  def considerDiagrams(boundary: Int, vertices: Int): Seq[SpiderData] = {
    println(s"Considering diagrams with $boundary boundary points and $vertices vertices...")

    val newConsideredDiagramsVertexBound = consideredDiagramsVertexBound.padTo(boundary + 1, 0).updated(boundary, vertices)
    val diagramsToConsider = spider.reducedDiagrams(boundary, vertices)
    for (d <- diagramsToConsider) println("   " + d)

    diagramsToConsider.foldLeft(Seq(this))({ (s: Seq[SpiderData], p: PlanarGraph) => s.flatMap(d => d.considerDiagram(p)).filter(s => s.independentDiagrams.lift(6).getOrElse(Seq.empty).size == s.consideredDiagrams.lift(6).getOrElse(Seq.empty).size) })
      .map(_.copy(consideredDiagramsVertexBound = newConsideredDiagramsVertexBound))
  }

  def considerDiagrams(diagramSizes: Seq[(Int, Int)]): Seq[SpiderData] = {
    diagramSizes.foldLeft(Seq(this))({ (data: Seq[SpiderData], step: (Int, Int)) =>
      val result = data.flatMap(_.considerDiagrams(step._1, step._2))
      for (s <- result) println(s)
      result
    })
  }
}

object InvestigateTetravalentSpiders extends App {
  val lowestWeightTetravalentSpider = (new LowestWeightSpider {
    override def generators = Seq((VertexType(4, 1), ring.one))
  }).asQuotientSpider

  val invertible = Seq(MultivariablePolynomial(Map(Map("p1" -> 1) -> Fraction[BigInt](1, 1))),
    MultivariablePolynomial(Map(Map("p2" -> 1) -> Fraction[BigInt](1, 1))),
    MultivariablePolynomial(Map(Map[String, Int]() -> Fraction[BigInt](1, 1), Map("p1" -> 1) -> Fraction[BigInt](1, 1))),
    MultivariablePolynomial(Map(Map[String, Int]() -> Fraction[BigInt](-1, 1), Map("p1" -> 1) -> Fraction[BigInt](1, 1))) //,
    //      MultivariablePolynomial(Map(Map() -> Fraction[BigInt](2, 1), Map("p1" -> 1) -> Fraction[BigInt](1, 1))),
    //      MultivariablePolynomial(Map(Map() -> Fraction[BigInt](-2, 1), Map("p1" -> 1) -> Fraction[BigInt](1, 1))),
    //      MultivariablePolynomial(Map(Map() -> Fraction[BigInt](-2, 1), Map("p1" -> 2) -> Fraction[BigInt](1, 1))) //
    )

  val initialData = invertible.foldLeft(SpiderData(
    lowestWeightTetravalentSpider,
    Seq.empty,
    Seq.empty,
    dimensionBounds = Seq(1, 0, 1, 0, 3, 0, 14),
    Seq.empty,
    Seq.empty,
    Seq.empty,
    Seq.empty))(_.invertPolynomial(_).get._1)

  val steps = Seq((0, 0), (2, 0), (0, 1), (0, 2), (2, 1), (2, 2), (2, 3), (4, 0), (4, 1), (4, 2), (4, 3), (6, 0), (6, 1) /*, (6, 2)*/ )

  // TODO start computing relations, also

  val results = initialData.considerDiagrams(steps)

  //  for (r <- results) {
  //    println(r.innerProducts(r.consideredDiagrams(6)).toMathematicaInputString)
  //  }

  println(results.size)
}