package net.tqft.toolkit.algebra.grouptheory

import net.tqft.toolkit.algebra._
import net.tqft.toolkit.algebra.polynomials._
import net.tqft.toolkit.algebra.matrices._
import scala.collection.GenSet
import scala.collection.GenSeq

object FiniteGroup extends net.tqft.toolkit.Logging

trait FiniteGroup[A] extends Group[A] with Elements[A] { finiteGroup =>

  def verifyInverses = {
    for (x <- elements) {
      require(multiply(x, inverse(x)) == one)
      require(multiply(inverse(x), x) == one)
    }
    true
  }

  def verifyAssociativity = {
    for (x <- elements; y <- elements; z <- elements) {
      require(multiply(multiply(x, y), z) == multiply(x, multiply(y, z)))
    }
    true
  }

  def verifySubgroup(subgroup: Set[A]) = {
    for (x <- subgroup) {
      require(subgroup.contains(inverse(x)))
    }
    for (x <- subgroup; y <- subgroup) {
      require(subgroup.contains(multiply(x, y)))
    }
    true
  }

  def verifyNormalSubgroup(subgroup: Set[A]) = {
    verifySubgroup(subgroup)
    for (x <- subgroup; y <- elements) {
      val p = multiply(multiply(inverse(y), x), y)
      require(subgroup.contains(p))
    }
    true
  }

  private class Subgroup(val elements: Set[A]) extends FiniteGroup[A] {
    override def one = finiteGroup.one
    override def inverse(a: A) = finiteGroup.inverse(a)
    override def multiply(a: A, b: A) = finiteGroup.multiply(a, b)
  }
  @scala.annotation.tailrec
  private def extendElements(generators: Set[A], elements: Set[A], newestElements: GenSet[A]): Set[A] = {
    FiniteGroup.info("... found " + elements.size + " elements of the subgroup so far.")
    if (newestElements.isEmpty) {
      elements
    } else {
      val allElements = elements union newestElements;
      extendElements(generators, allElements, (for (b <- newestElements; a <- generators) yield finiteGroup.multiply(a, b)) diff allElements)
    }
  }

  private class FinitelyGeneratedSubgroup(val generators: Set[A]) extends Subgroup({
    FiniteGroup.info("Enumerating the subgroup generated by " + generators)
    extendElements(generators, Set(finiteGroup.one), generators.par)
  }) with FinitelyGeneratedFiniteGroup[A]

  def subgroup(elements: Set[A]): FiniteGroup[A] = new Subgroup(elements)
  def subgroupGeneratedBy(generators: Set[A]): FinitelyGeneratedFiniteGroup[A] = new FinitelyGeneratedSubgroup(generators)

  protected def unsortedConjugacyClasses = GroupActions.conjugationAction(finiteGroup).orbits(elements, elements).toSeq

  lazy val conjugacyClasses = unsortedConjugacyClasses.sortBy({ c => (c.representative != one, c.elements.size) })
  lazy val conjugacyClassOrders = for (c <- conjugacyClasses) yield orderOfElement(c.representative)
  private def exponentationOnConjugacyClassesImpl(k: Int) = conjugacyClasses.map({ cx => conjugacyClasses.indexWhere({ cy => cy.elements.contains(power(cx.representative, k)) }) }).toIndexedSeq
  private lazy val primeExponentiationOnConjugacyClasses: (Int => IndexedSeq[Int]) = {
    import net.tqft.toolkit.arithmetic.Primes
    Primes.takeWhile({ n => n < exponent }).map({ n => n -> exponentationOnConjugacyClassesImpl(n) }).toMap
  }
  lazy val exponentiationOnConjugacyClasses: Int => IndexedSeq[Int] = {
    def impl(k: Int) = {
      if (k == 0) {
        IndexedSeq.fill(conjugacyClasses.size)(0)
      } else {
        import net.tqft.toolkit.arithmetic.Factor
        import net.tqft.toolkit.permutations.Permutations.Permutation2RichPermutation
        Factor(k).map(primeExponentiationOnConjugacyClasses(_)).fold(0 until conjugacyClasses.size)({ (p: IndexedSeq[Int], q: IndexedSeq[Int]) => p permute q })
      }
    }
    (for(n <- 0 until exponent) yield n -> impl(n)).toMap
  }
  lazy val inverseOnConjugacyClasses = exponentationOnConjugacyClassesImpl(-1)
  lazy val inverseConjugacyClasses = {
    import net.tqft.toolkit.permutations.Permutations.Permutation2RichPermutation
    inverseOnConjugacyClasses permute conjugacyClasses
  }

  val classCoefficients: Int => Seq[Seq[Int]] = {
    def impl(ix: Int) = {
      val cx = inverseConjugacyClasses(ix)
      (for ((cy, iy) <- conjugacyClasses.zipWithIndex.par) yield {
        FiniteGroup.info("Computing class coefficient " + (ix, iy))
        for (cz <- conjugacyClasses; z = cz.representative) yield {
          cx.elements.count({ x => cy.elements.contains(multiply(x, z)) })
        }
      }).seq
    }
    import net.tqft.toolkit.functions.Memo._
    (impl _).memo
  }

  lazy val exponent = Gadgets.Integers.lcm((elements map { orderOfElement _ }).toSeq: _*)

  lazy val preferredPrime = {
    var p = exponent + 1
    while (!BigInt(p).isProbablePrime(60)) p = p + exponent
    p
  }

  trait Character[F] {
    def field: Field[F]
    def character: Seq[F]
    def degree: Int
  }
  object Character {
//    import language.implicitConversions
    implicit def liftRationals(x: Seq[Fraction[Int]]): RationalCharacter = new RationalCharacter {
      override val character = x
    }
    implicit def liftIntegers(x: Seq[Int]): RationalCharacter = new RationalCharacter {
      override val character = x.map(Gadgets.Rationals.fromInt(_))
    }
  }

  trait RationalCharacter extends Character[Fraction[Int]] {
    override def field = Gadgets.Rationals
    override def degree = character.head.ensuring(_.denominator == 1).numerator
  }
  trait CyclotomicCharacter extends Character[Polynomial[Fraction[Int]]] {
    def order: Int
    override def field = NumberField.cyclotomic(order)(Gadgets.Rationals)
    override def degree = character.head.ensuring(_.maximumDegree.getOrElse(0) == 0).constantTerm(Gadgets.Rationals).ensuring(_.denominator == 1).numerator
  }

  lazy val characters: Seq[Seq[Polynomial[Fraction[Int]]]] = {
    val k = conjugacyClasses.size

    def classCoefficientSimultaneousEigenvectorsModPrime = {
      import net.tqft.toolkit.arithmetic.Mod._
      implicit val modP = Mod(preferredPrime)
      val zeroVector = Seq.fill(k)(0)

      def subtractDiagonal(m: Seq[Seq[Int]], lambda: Int) = {
        m.zipWithIndex.map({ case (r, i) => r.updated(i, (r(i) - lambda) mod preferredPrime) })
      }

      def eigenvalues(m: Seq[Seq[Int]]) = new Matrix(m.size, m.par).eigenvalues

      case class PartialEigenspace(annihilators: GenSeq[Seq[Int]], eigenvalues: Seq[Int], eigenvectors: Option[Seq[Seq[Int]]]) {
        def splitAlong(m: => Seq[Seq[Int]], mEigenvalues: => Set[Int]): Set[PartialEigenspace] = {
          eigenvectors.map(_.size) match {
            case Some(0) => Set()
            case Some(1) => {
              Set(this)
            }
            case _ => {
              for (lambda <- mEigenvalues) yield {
                val newAnnihilators = new Matrix(conjugacyClasses.size, annihilators ++ subtractDiagonal(m, lambda)).rowEchelonForm(modP)
                PartialEigenspace(newAnnihilators.entries, eigenvalues :+ lambda, Some(newAnnihilators.nullSpace))
              }
            }
          }
        }
      }

      val cachedEigenvalues = {
        def impl(n: Int) = new Matrix(k, classCoefficients(n).par).eigenvalues
        import net.tqft.toolkit.functions.Memo._
        (impl _).memo
      }

      val unnormalizedEigenvectors = (1 until k).foldLeft(Set(PartialEigenspace(Seq().par, Seq(), None)))({ case (s, i) => s.flatMap({ p => p.splitAlong(classCoefficients(i), cachedEigenvalues(i)) }) }).flatMap(_.eigenvectors.get).toSeq
      FiniteGroup.info("Found simultaneous eigenvectors.")

      val normalizedEigenvectors = unnormalizedEigenvectors.map({ v => v.map({ x => modP.quotient(x, v(0)) }) })

      normalizedEigenvectors
    }

    def characterTableModPreferredPrime = {
      implicit val modP = Mod(preferredPrime)

      def sqrt(x: Int) = {
        import net.tqft.toolkit.arithmetic.Mod._
        (0 to preferredPrime / 2).find({ n => ((n * n - x) mod preferredPrime) == 0 }).get
      }

      val omega = classCoefficientSimultaneousEigenvectorsModPrime
      val degrees = for (omega_i <- omega) yield {
        sqrt(modP.quotient(finiteGroup.size, (for (j <- 0 until k) yield modP.quotient(omega_i(j) * omega_i(inverseOnConjugacyClasses(j)), conjugacyClasses(j).size)).sum))
      }

      for (i <- 0 until k) yield for (j <- 0 until k) yield modP.quotient(omega(i)(j) * degrees(i), conjugacyClasses(j).size)
    }

    implicit val rationals = Gadgets.Rationals
    implicit val modP = Mod(preferredPrime)

    val cyclotomicNumbers = NumberField.cyclotomic(exponent)(rationals)
    val zeta = Polynomial.identity(rationals)
    val chi = characterTableModPreferredPrime
    val z = (1 until preferredPrime).find({ n => modP.orderOfElement(n) == exponent }).get

    val zpower = IndexedSeq.tabulate(exponent)({ k: Int => modP.power(z, k) })
    val zetapower = IndexedSeq.tabulate(exponent)({ k: Int => cyclotomicNumbers.power(zeta, k) })

    import net.tqft.toolkit.arithmetic.Mod._

    // ACHTUNG!
    // make sure we don't deadlock (c.f. https://issues.scala-lang.org/browse/SI-5808)
    exponentiationOnConjugacyClasses(0)

    def mu(i: Int)(j: Int)(s: Int) = modP.quotient(modP.add(for (n <- 0 until exponent) yield modP.multiply(chi(i)(exponentiationOnConjugacyClasses(n)(j)), zpower((-s * n) mod exponent))), exponent)

    val unsortedCharacters = (for (i <- (0 until k).par) yield (for (j <- (0 until k).par) yield {
      FiniteGroup.info("Computing entry " + (i, j) + " in the character table.")
      cyclotomicNumbers.add(for (s <- 0 until exponent) yield cyclotomicNumbers.multiplyByInt(zetapower(s), mu(i)(j)(s)))
    }).seq).seq
    val sortedCharacters = unsortedCharacters.sortBy({ v => (v(0).constantTerm, v.tail.headOption.map({ p => rationals.negate(p.constantTerm) })) })

    sortedCharacters
  }

  def reducedCharacters: Seq[Character[_]] = {
    for (c <- characters) yield {
      if (c.map(_.maximumDegree.getOrElse(0)).max == 0) {
        new RationalCharacter {
          val character = c.map(_.constantTerm)
        }
      } else {
        // TODO really should find the smallest cyclotomic field
        // see http://www.math.ru.nl/~bosma/pubs/AAECC1990.pdf
        new CyclotomicCharacter {
          val order = exponent
          val character = c
        }
      }
    }
  }
  def rationalCharacters: Seq[RationalCharacter] = reducedCharacters collect { case c: RationalCharacter => c }

  def characterPairing[M, N](m: Character[M], n: Character[N]): Int = {
    def liftCharacterToCyclotomicFieldOfExponent(c: Character[_]) = {
      val polynomials = Polynomials.over(Gadgets.Rationals)
      new CyclotomicCharacter {
        override val order = exponent
        override val character = c match {
          case c: RationalCharacter => c.character.map({ x => polynomials.constant(x) })
          case c: CyclotomicCharacter => {
            val power = exponent / c.order
            c.character.map({ p => field.normalize(polynomials.composeAsFunctions(p, polynomials.monomial(power))) })
          }
        }
      }
    }
    val Q = NumberField.cyclotomic[Fraction[Int]](exponent)
    val result = Q.quotientByInt(
      Q.add(
        for (((a, b), t) <- liftCharacterToCyclotomicFieldOfExponent(m).character zip liftCharacterToCyclotomicFieldOfExponent(n).character zip conjugacyClasses.map(_.size)) yield {
          Q.multiplyByInt(Q.multiply(a, Q.bar(b)), t)
        }),
      finiteGroup.size)
    require(result.maximumDegree.getOrElse(0) == 0)
    val rationalResult = result.constantTerm
    require(rationalResult.denominator == 1)
    rationalResult.numerator
  }

  // TODO rewrite this in terms of other stuff!
  lazy val tensorProductMultiplicities: Seq[Seq[Seq[Int]]] = {
    val k = conjugacyClasses.size
    implicit val Q = NumberField.cyclotomic[Fraction[Int]](exponent)

    def pairing(x: Seq[Polynomial[Fraction[Int]]], y: Seq[Polynomial[Fraction[Int]]]) = {
      Q.quotientByInt(Q.add((x zip y zip conjugacyClasses.map(_.size)).map({ p => Q.multiplyByInt(Q.multiply(p._1._1, p._1._2), p._2) })), finiteGroup.size)
    }

    def lower(p: Polynomial[Fraction[Int]]) = {
      require(p == Q.zero || p.maximumDegree == Some(0))
      val c = p.constantTerm
      require(c.denominator == 1)
      c.numerator
    }

    // hmm, deadlocks; maybe this will make sure they don't happen!
    characters(0)

    (for (i <- (0 until k).par) yield {
      FiniteGroup.info("Computing tensor product multiplicities (" + i + ", *)")
      (for (j <- (0 until k).par) yield {
        val product = (characters(i) zip characters(j)).map({ p => Q.multiply(p._1, p._2) }) map (Q.bar _)
        for (c <- characters) yield lower(pairing(product, c))
      }).seq
    }).seq
  }
}

trait FinitelyGeneratedFiniteGroup[A] extends FiniteGroup[A] { fgFiniteGroup =>
  def generators: Set[A]
  override def unsortedConjugacyClasses = GroupActions.conjugationAction(fgFiniteGroup).orbits(generators, elements).toSeq
}

trait FiniteGroupHomomorphism[A, B] extends Homomorphism[FiniteGroup, A, B] { homomorphism =>
  def kernel = source.subgroup(source.elements.par.filter(homomorphism(_) == target.one).seq)
}


