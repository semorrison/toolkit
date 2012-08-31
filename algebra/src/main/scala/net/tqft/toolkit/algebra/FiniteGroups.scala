package net.tqft.toolkit.algebra

import scala.collection.immutable.HashSet
import scala.collection.GenSeq
import scala.collection.GenSet
import net.tqft.toolkit.Logging
import net.tqft.toolkit.permutations.Permutation
import net.tqft.toolkit.algebra.polynomials.Polynomials
import net.tqft.toolkit.algebra.polynomials.Polynomial
import net.tqft.toolkit.algebra.matrices._

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

      //      def proportional(w: Seq[Int], v: Seq[Int]) = {
      //        if (v == zeroVector) {
      //          w == zeroVector
      //        } else {
      //          val ratios = (w zip v).filter(_ != (0, 0))
      //          if (ratios.indexWhere({ p => p._2 == 0 }) < 0) {
      //            ratios.map({ p => modP.quotient(p._1, p._2) }).distinct.size == 1
      //          } else {
      //            false
      //          }
      //        }
      //      }
      //
      //    for (v <- normalizedEigenvectors; m0 <- classCoefficients; w = new Matrix(m0.size, m0).apply(v)(modP)) {
      //      require(proportional(w, v))
      //    }
      //
      //    require(new Matrix(conjugacyClasses.size, normalizedEigenvectors).rank() == conjugacyClasses.size)

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
    //    def order(n: Int) = (1 until preferredPrime).find({ m => modP.power(n, m) == 1 }).get
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

object FiniteGroup extends Logging

trait FinitelyGeneratedFiniteGroup[A] extends FiniteGroup[A] { fgFiniteGroup =>
  def generators: Set[A]
  override def unsortedConjugacyClasses = GroupActions.conjugationAction(fgFiniteGroup).orbits(generators, elements).toSeq
}

trait EquivalenceClass[A] extends Elements[A] {
  def representative: A
  def contains(a: A) = elements.contains(a)

  def leastRepresentative(implicit o: Ordering[A]) = elements.min

  override def equals(other: Any) = {
    other match {
      case other: EquivalenceClass[_] => {
        contains(other.asInstanceOf[EquivalenceClass[A]].representative)
      }
      case _ => false
    }
  }
  override def hashCode = {
    elements.hashCode
  }
}

object EquivalenceClass {
  implicit def equivalenceClassOrdering[A](implicit o: Ordering[A]): Ordering[EquivalenceClass[A]] = new Ordering[EquivalenceClass[A]] {
    def compare(x: EquivalenceClass[A], y: EquivalenceClass[A]) = o.compare(x.leastRepresentative, y.leastRepresentative)
  }
}

trait FiniteGroupHomomorphism[A, B] extends Homomorphism[FiniteGroup, A, B] { homomorphism =>
  def kernel = source.subgroup(source.elements.par.filter(homomorphism(_) == target.one).seq)
}

object FiniteGroups {

  def trivialGroup[A](one: A): FiniteGroup[A] = new TrivialGroup(one)
  def cyclicGroup(n: Int): FiniteGroup[Int] = new CyclicGroup(n)
  def dihedralGroup(n: Int): FiniteGroup[(Int, Boolean)] = {
    if (n == 0) {
      new TrivialGroup((0, false))
    } else {
      new DihedralGroup(n)
    }
  }

  trait LeftCoset[A] extends EquivalenceClass[A] {
    def group: FiniteGroup[A]
    def stabilizer: FiniteGroup[A]

    override def contains(a: A) = stabilizer.elements.contains(group.multiply(group.inverse(a), representative))

    def elements = (stabilizer.elements.map { group.multiply(representative, _) })
    override def toString = elements.mkString("LeftCoset[", ", ", "]")
  }

  trait DoubleCoset[A] extends EquivalenceClass[A] {
    def group: FiniteGroup[A]
    def leftStabilizer: FiniteGroup[A]
    def rightStabilizer: FiniteGroup[A]

    // TODO is there a better implementation of contains than the one inherited from EquivalenceClass? 
    // def contains(a: A) = elements.contains(a)

    def elements = for (h <- leftStabilizer.elements; g <- rightStabilizer.elements; x = representative) yield group.multiply(h, group.multiply(x, g))
    override def toString = elements.mkString("DoubleCoset[", ", ", "]")
  }

  def leftCosets[A](_group: FiniteGroup[A], subgroup: FiniteGroup[A]): Set[LeftCoset[A]] = {
    class C(val representative: A) extends LeftCoset[A] {
      def group = _group
      def stabilizer = subgroup
    }

    def extractCosets(elements: Set[A], cosets: Set[LeftCoset[A]]): Set[LeftCoset[A]] = {
      if (elements.isEmpty) {
        cosets
      } else {
        val newCoset = new C(elements.head)
        extractCosets(elements -- newCoset.elements, cosets + newCoset)
      }
    }
    val result = extractCosets(_group.elements, Set())
    result ensuring { _.size == _group.elements.size / subgroup.elements.size }
  }

  def doubleCosets[A](_group: FiniteGroup[A], leftSubgroup: FiniteGroup[A], rightSubgroup: FiniteGroup[A]): Set[DoubleCoset[A]] = {
    class D(val representative: A) extends DoubleCoset[A] {
      def group = _group
      def leftStabilizer = leftSubgroup
      def rightStabilizer = rightSubgroup
    }

    def extractCosets(elements: Set[A], cosets: Set[DoubleCoset[A]]): Set[DoubleCoset[A]] = {
      if (elements.isEmpty) {
        cosets
      } else {
        val newCoset = new D(elements.head)
        extractCosets(elements -- newCoset.elements, cosets + newCoset)
      }
    }
    extractCosets(_group.elements, Set())
  }

  def quotient[A](_group: FiniteGroup[A], normalSubgroup: FiniteGroup[A]): FiniteGroup[LeftCoset[A]] = {
    class C(val representative: A) extends LeftCoset[A] {
      def group = _group
      def stabilizer = normalSubgroup
    }

    new FiniteGroup[LeftCoset[A]] {
      override def one = new C(_group.one)
      override def inverse(a: LeftCoset[A]) = new C(_group.inverse(a.representative))
      override def multiply(a: LeftCoset[A], b: LeftCoset[A]) = new C(_group.multiply(a.representative, b.representative))

      override def elements = leftCosets(_group, normalSubgroup)

    }
  }

  private class TrivialGroup[A](identity: A) extends FiniteGroup[A] {
    override def one = identity
    override def inverse(a: A) = identity
    override def multiply(a: A, b: A) = identity

    override val elements = Set(identity)

  }

  // this is the dihedral group with n elements
  private class DihedralGroup(n: Int) extends FiniteGroup[(Int, Boolean)] {
    require(n > 0 && n % 2 == 0)

    val k = n / 2
    import net.tqft.toolkit.arithmetic.Mod._

    override def one = (0, false)
    override def inverse(a: (Int, Boolean)) = (if (a._2) a._1 else (-a._1 mod k), a._2)
    override def multiply(a: (Int, Boolean), b: (Int, Boolean)) = ((a._1 + (if (a._2) -b._1 else b._1)) mod k, a._2 ^ b._2)

    override val elements = (for (j <- Set(false, true); i <- 0 until k) yield (i, j))

  }
  private class CyclicGroup(n: Int) extends FiniteGroup[Int] {
    import net.tqft.toolkit.arithmetic.Mod._

    override def one = 0
    override def inverse(a: Int) = -a mod n
    override def multiply(a: Int, b: Int) = (a + b) mod n

    override val elements = (0 until n).toSet
  }
  import net.tqft.toolkit.permutations.Permutation
  private class PermutationGroup(n: Int) extends FiniteGroup[IndexedSeq[Int]] {
    import net.tqft.toolkit.permutations.Permutations
    import net.tqft.toolkit.permutations.Permutations.Permutation2RichPermutation
    override def elements = Permutations.of(n).toSet
    override def inverse(x: IndexedSeq[Int]) = Permutations.inverse(x)
    override def multiply(x: IndexedSeq[Int], y: IndexedSeq[Int]) = x permute y
    override def one = 0 until n
  }

  val symmetricGroup: Int => FiniteGroup[Permutation] = {
    import net.tqft.toolkit.functions.Memo._
    { n => new PermutationGroup(n) }.memo
  }

  def signature(n: Int): FiniteGroupHomomorphism[IndexedSeq[Int], Int] = new FiniteGroupHomomorphism[IndexedSeq[Int], Int] {
    val source = symmetricGroup(n)
    val target = cyclicGroup(2)
    def apply(p: IndexedSeq[Int]) = {
      var k = 0
      for (i <- 0 until p.size; j <- 0 until i; if p(i) < p(j)) k = k + 1
      k % 2
    }
  }

  val alternatingGroup: Int => FiniteGroup[Permutation] = {
    import net.tqft.toolkit.functions.Memo._
    { n: Int => signature(n).kernel }.memo
  }
  val signedPermutationGroup: Int => FiniteGroup[(Seq[Int], Permutation)] = {
    import net.tqft.toolkit.functions.Memo._
    { k: Int => semidirectProduct(power(cyclicGroup(2), k), symmetricGroup(k), GroupActions.permutationAction[Int]) }.memo
  }

  lazy val Mathieu11 = symmetricGroup(11).subgroupGeneratedBy(Set(
    IndexedSeq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0),
    IndexedSeq(0, 1, 6, 9, 5, 3, 10, 2, 8, 4, 7)))
  lazy val Mathieu12 = symmetricGroup(12).subgroupGeneratedBy(Set(
    IndexedSeq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 11),
    IndexedSeq(0, 1, 6, 9, 5, 3, 10, 2, 8, 4, 7, 11),
    IndexedSeq(11, 10, 5, 7, 8, 2, 9, 3, 4, 6, 1, 0)))
  lazy val Mathieu22 = symmetricGroup(22).subgroupGeneratedBy(Set(
    IndexedSeq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 11),
    IndexedSeq(3, 7, 0, 4, 8, 1, 5, 9, 2, 6, 10, 14, 18, 11, 15, 19, 12, 16, 20, 13, 17, 21),
    IndexedSeq(20, 9, 12, 16, 18, 1, 6, 5, 17, 7, 21, 13, 3, 15, 14, 19, 2, 4, 8, 11, 0, 10)))
  lazy val Mathieu23 = symmetricGroup(23).subgroupGeneratedBy(Set(
    IndexedSeq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 0),
    IndexedSeq(0, 1, 16, 12, 3, 5, 8, 17, 2, 6, 11, 22, 13, 18, 19, 14, 9, 10, 4, 21, 15, 20, 7)))
  lazy val Mathieu24 = symmetricGroup(24).subgroupGeneratedBy(Set(
    IndexedSeq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 0, 23),
    IndexedSeq(0, 1, 16, 12, 3, 5, 8, 17, 2, 6, 11, 22, 13, 18, 19, 14, 9, 10, 4, 21, 15, 20, 7, 23),
    IndexedSeq(23, 22, 11, 15, 17, 9, 19, 13, 20, 5, 16, 2, 21, 7, 18, 3, 10, 4, 14, 6, 8, 12, 1, 0)))

  def semidirectProduct[A, B](group1: FiniteGroup[A], group2: FiniteGroup[B], action: GroupAction[B, A]): FiniteGroup[(A, B)] = {
    new FiniteGroup[(A, B)] {
      override def elements = for (g1 <- group1.elements; g2 <- group2.elements) yield (g1, g2)
      override def one = (group1.one, group2.one)
      override def inverse(x: (A, B)) = {
        val bInverse = group2.inverse(x._2)
        (action.act(bInverse, group1.inverse(x._1)), bInverse)
      }
      override def multiply(x: (A, B), y: (A, B)) = (group1.multiply(x._1, action.act(x._2, y._1)), group2.multiply(x._2, y._2))

    }
  }

  def product[A, B](group1: FiniteGroup[A], group2: FiniteGroup[B]) = semidirectProduct(group1, group2, GroupActions.trivialAction)

  def power[A](group: FiniteGroup[A], k: Int): FiniteGroup[Seq[A]] = {
    new FiniteGroup[Seq[A]] {
      override def elements = {
        def combine[A](xs: Traversable[Traversable[A]]): Seq[Seq[A]] =
          xs.foldLeft(Seq(Seq.empty[A])) {
            (x, y) => for (a <- x.view; b <- y) yield a :+ b
          }

        combine(Seq.fill(k)(group.elements)).toSet
      }
      override def one = Seq.fill(k)(group.one)
      override def inverse(x: Seq[A]) = x.map(group.inverse _)
      override def multiply(x: Seq[A], y: Seq[A]) = (x zip y).map({ case (xg, yg) => group.multiply(xg, yg) })

    }
  }
}

trait GroupAction[A, B] {
  def act(a: A, b: B): B
  def orbits(generators: Set[A], objects: Set[B]): Set[Orbit[A, B]] = {
    class O(val representative: B) extends Orbit[A, B] {
      override def stabilizer = ???
      override lazy val elements = extendElements(HashSet.empty[B], Set(representative).par)

      @scala.annotation.tailrec
      private def extendElements(elements: Set[B], newestElements: GenSet[B]): Set[B] = {
        if (newestElements.isEmpty) {
          elements
        } else {
          val allElements = elements union newestElements;
          extendElements(allElements, (for (b <- newestElements; a <- generators) yield act(a, b)) diff allElements)
        }
      }
    }

    def extractOrbits(objects: Set[B], orbits: Set[Orbit[A, B]]): Set[Orbit[A, B]] = {
      if (objects.isEmpty) {
        orbits
      } else {
        val newOrbit = new O(objects.head)
        extractOrbits(objects diff newOrbit.elements, orbits + newOrbit)
      }
    }

    extractOrbits(objects, Set())
  }
}

trait Orbit[A, B] extends EquivalenceClass[B] {
  def stabilizer: FiniteGroup[A]
}

object GroupActions {
  def trivialAction[A, B]: GroupAction[A, B] = new GroupAction[A, B] {
    def act(a: A, b: B) = b
  }
  def permutationAction[C]: GroupAction[Permutation, Seq[C]] = new GroupAction[Permutation, Seq[C]] {
    import net.tqft.toolkit.permutations.Permutations.Permutation2RichPermutation

    def act(a: Permutation, b: Seq[C]) = a permute b
  }
  def conjugationAction[A](group: Group[A]) = new GroupAction[A, A] {
    def act(a: A, b: A) = group.multiply(group.inverse(a), b, a)
  }
}

trait Representation[A, F] extends Homomorphism[Group, A, Matrix[F]] { representation =>
  def degree: Int
  override val source: FiniteGroup[A]
  override def target = ??? // GL(F, n)?

  def character(implicit addition: CommutativeMonoid[F]): Seq[F] = {
    for (c <- source.conjugacyClasses; g = c.representative) yield {
      apply(g).trace
    }
  }

  def irrepMultiplicities(implicit evidence: F =:= Fraction[Int]): Seq[Int] = {
    val chi = character(Gadgets.Rationals.asInstanceOf[Field[F]]).map(evidence)
    for (c <- source.reducedCharacters) yield {
      source.characterPairing(chi, c)
    }
  }
  def basisForIsotypicComponent(chi: source.RationalCharacter)(implicit field: Field[F], manifest: ClassManifest[F]): Seq[Seq[F]] = {
    require(chi.character.size == source.conjugacyClasses.size)
    val matrices = Matrices.matricesOver(degree)(field)
    var j = 0
    def blip {
      j = j + 1
      if (j % 1000 == 0) println(j / 1000)
    }
    def volatileZeroMatrix = {
      import net.tqft.toolkit.collections.SparseSeq
      matrices.zero.par.mapRows({ r => r.toArray[F] })
    }
    def projectorConjugacyClass(cc: Set[A], c: Fraction[Int]) = {
      val result = matrices.scalarMultiply(field.fromRational(c), cc.foldLeft(volatileZeroMatrix)({ (m: Matrix[F], g: A) => blip; matrices.add(m, representation(g)) }))
      require(result.entries.head.isInstanceOf[scala.collection.mutable.WrappedArray[_]])
      result
    }
    val projector = (source.conjugacyClasses zip chi.character).foldLeft(volatileZeroMatrix)({
      (m: Matrix[F], p: (Orbit[A, A], Fraction[Int])) =>
        {
          Representation.info("Preparing projector to the " + chi.character + " isotypic component in conjugacy class " + p._1.representative)
          matrices.add(m, projectorConjugacyClass(p._1.elements, p._2))
        }
    }).ensuring(_.entries.head.isInstanceOf[scala.collection.mutable.WrappedArray[_]]).mapRows(_.toList)
    //    val chidegree = (chi.character.head.ensuring(_.denominator == 1)).numerator
    //    val eigenvalue = field.quotientByInt(field.fromInt(source.size), chidegree)
    //    projector.eigenspace(eigenvalue)
    projector.par.rowEchelonForm.entries.filter({ x => x.find(_ != field.zero).nonEmpty }).seq
  }
}

object Representation extends Logging
object Representations {
  private def elementaryVector[A](entry: A, zero: A, n: Int, k: Int) = Seq.fill(k)(zero) ++ (entry +: Seq.fill(n - k - 1)(zero))
  private def unitVector[A: Zero: One](n: Int, k: Int) = elementaryVector(implicitly[One[A]].one, implicitly[Zero[A]].zero, n, k)

  def permutationRepresentation[F: Ring](n: Int): Representation[Permutation, F] = permutationRepresentation(FiniteGroups.symmetricGroup(n))
  def permutationRepresentation[F: Ring](_source: FiniteGroup[Permutation]): Representation[Permutation, F] = {
    new Representation[Permutation, F] {
      override val degree = _source.one.size
      override val source = _source
      override def apply(p: Permutation) = {
        import net.tqft.toolkit.collections.SparseSeq
        new Matrix(degree, for (k <- p) yield {
          SparseSeq.elementaryVector[F](degree, k, implicitly[Ring[F]].one, implicitly[Ring[F]].zero)
        })
      }
    }
  }
  def signedPermutationRepresentation[F: Ring](n: Int): Representation[(Seq[Int], Permutation), F] = signedPermutationRepresentation(FiniteGroups.signedPermutationGroup(n))
  def signedPermutationRepresentation[F: Ring](source: FiniteGroup[(Seq[Int], Permutation)]): Representation[(Seq[Int], Permutation), F] = {
    val _source = source
    new Representation[(Seq[Int], Permutation), F] {
      override val source = _source
      override val degree = _source.one._1.size
      override def apply(p: (Seq[Int], Permutation)) = {
        import net.tqft.toolkit.collections.SparseSeq
        val ring = implicitly[Ring[F]]
        import net.tqft.toolkit.collections.Pairs._
        new Matrix(degree, for ((s, k) <- p.transpose) yield {
          SparseSeq.elementaryVector[F](degree, k, if (s == 0) ring.one else ring.fromInt(-1), ring.zero)
        })
      }
    }
  }

  def tensor[A, F: Ring](V: Representation[A, F], W: Representation[A, F]): Representation[A, F] = {
    require(V.source == W.source)
    new Representation[A, F] {
      override val source = V.source
      override def degree = V.degree * W.degree
      override def apply(a: A) = Matrices.tensor(V(a), W(a))
    }
  }

  def tensorPower[A, F: Ring](V: Representation[A, F], k: Int): Representation[A, F] = {
    new Representation[A, F] {
      override val source = V.source
      override def degree = Gadgets.Integers.power(V.degree, k)
      override def apply(a: A) = {
        val Va = V(a)
        Seq.fill(k)(Va).reduce(Matrices.tensor(_, _))
      }
    }
  }
}