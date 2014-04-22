package net.tqft.toolkit.algebra.grouptheory

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._

@RunWith(classOf[JUnitRunner])
class MathieuGroupsTest extends FlatSpec with Matchers {

  val M_12 = FiniteGroups.Mathieu12

  "M_12" should "be a group with 95040 elements" in {
    M_12.size should equal(95040)
  }
  "M_12" should "have 15 conjugacy classes" in {
    M_12.conjugacyClasses.size should equal(15)
  }
  "conjugacyClasses" should "return the class of the identity first" in {
    val c = M_12.conjugacyClasses.head
    c.size should equal(1)
    c.representative should equal(M_12.one)
  }

  "M_12" should "have the right exponent" in {
    M_12.exponent should equal(1320)
  }
  "M_12" should "compute the character table without choking" in {
    println(M_12.characters)
  }
  "M_12" should "have the right tensor product multiplicities" in {
    M_12.tensorProductMultiplicities should equal (Seq(Seq(Seq(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), Seq(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), Seq(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), Seq(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), Seq(0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)), Seq(Seq(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), Seq(1, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1), Seq(0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1), Seq(0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 1), Seq(0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1), Seq(0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 1, 1, 1), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1), Seq(0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1), Seq(0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 2, 2), Seq(0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 3, 2, 2), Seq(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3), Seq(0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 4)), Seq(Seq(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0), Seq(1, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1), Seq(0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 1, 0, 1, 1), Seq(0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1), Seq(0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1), Seq(0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1), Seq(0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2, 2), Seq(0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 3, 2, 2), Seq(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3), Seq(0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 4)), Seq(Seq(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1), Seq(0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0), Seq(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0), Seq(0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1), Seq(0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 2, 1), Seq(0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2), Seq(0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2), Seq(0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2), Seq(0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 2, 2), Seq(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3), Seq(0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 4), Seq(0, 0, 0, 1, 0, 1, 2, 1, 1, 1, 2, 2, 3, 4, 4), Seq(0, 1, 1, 0, 0, 1, 1, 2, 2, 2, 2, 3, 4, 4, 5)), Seq(Seq(0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1), Seq(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0), Seq(0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0), Seq(0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1), Seq(0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 1, 1, 1, 2, 1), Seq(0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2), Seq(0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2), Seq(0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2), Seq(0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 2, 2), Seq(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3), Seq(0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 2, 2, 3, 4), Seq(0, 0, 0, 0, 1, 1, 2, 1, 1, 1, 2, 2, 3, 4, 4), Seq(0, 1, 1, 0, 0, 1, 1, 2, 2, 2, 2, 3, 4, 4, 5)), Seq(Seq(0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1), Seq(0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1), Seq(0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1), Seq(1, 0, 0, 1, 1, 2, 2, 1, 1, 1, 2, 3, 2, 3, 3), Seq(0, 0, 0, 1, 1, 2, 2, 0, 1, 1, 2, 2, 4, 4, 4), Seq(0, 0, 0, 0, 0, 1, 0, 1, 2, 2, 1, 3, 4, 3, 5), Seq(0, 1, 0, 0, 0, 1, 1, 2, 2, 2, 1, 3, 3, 3, 5), Seq(0, 0, 1, 0, 0, 1, 1, 2, 2, 2, 1, 3, 3, 3, 5), Seq(0, 0, 0, 1, 1, 2, 2, 1, 1, 1, 3, 3, 4, 5, 5), Seq(0, 0, 0, 1, 1, 3, 2, 3, 3, 3, 3, 5, 6, 6, 8), Seq(0, 1, 1, 1, 1, 2, 4, 4, 3, 3, 4, 6, 6, 8, 10), Seq(0, 1, 1, 1, 1, 3, 4, 3, 3, 3, 5, 6, 8, 11, 12), Seq(0, 1, 1, 1, 1, 3, 4, 5, 5, 5, 5, 8, 10, 12, 15)), Seq(Seq(0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0), Seq(0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 1), Seq(0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 1, 0, 1, 1), Seq(0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 2, 1), Seq(0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 1, 1, 1, 2, 1), Seq(0, 0, 0, 1, 1, 2, 2, 0, 1, 1, 2, 2, 4, 4, 4), Seq(1, 1, 1, 1, 1, 2, 4, 2, 1, 1, 3, 3, 2, 5, 5), Seq(0, 1, 1, 0, 0, 0, 2, 4, 2, 2, 2, 4, 2, 4, 6), Seq(0, 1, 0, 0, 0, 1, 1, 2, 3, 2, 1, 3, 4, 4, 6), Seq(0, 0, 1, 0, 0, 1, 1, 2, 2, 3, 1, 3, 4, 4, 6), Seq(0, 0, 0, 1, 1, 2, 3, 2, 1, 1, 4, 4, 4, 6, 6), Seq(0, 1, 1, 1, 1, 2, 3, 4, 3, 3, 4, 6, 6, 8, 10), Seq(0, 0, 0, 1, 1, 4, 2, 2, 4, 4, 4, 6, 10, 10, 12), Seq(0, 1, 1, 2, 2, 4, 5, 4, 4, 4, 6, 8, 10, 12, 14), Seq(0, 1, 1, 1, 1, 4, 5, 6, 6, 6, 6, 10, 12, 14, 18)), Seq(Seq(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0), Seq(0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1), Seq(0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1), Seq(0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2), Seq(0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2), Seq(0, 0, 0, 0, 0, 1, 0, 1, 2, 2, 1, 3, 4, 3, 5), Seq(0, 1, 1, 0, 0, 0, 2, 4, 2, 2, 2, 4, 2, 4, 6), Seq(1, 1, 1, 1, 1, 1, 4, 3, 1, 1, 3, 4, 2, 5, 5), Seq(0, 0, 0, 1, 1, 2, 2, 1, 1, 2, 3, 3, 4, 5, 5), Seq(0, 0, 0, 1, 1, 2, 2, 1, 2, 1, 3, 3, 4, 5, 5), Seq(0, 1, 1, 0, 0, 1, 2, 3, 3, 3, 2, 4, 4, 5, 7), Seq(0, 1, 1, 1, 1, 3, 4, 4, 3, 3, 4, 6, 6, 8, 10), Seq(0, 0, 0, 1, 1, 4, 2, 2, 4, 4, 4, 6, 11, 10, 12), Seq(0, 1, 1, 1, 1, 3, 4, 5, 5, 5, 5, 8, 10, 12, 15), Seq(0, 1, 1, 2, 2, 5, 6, 5, 5, 5, 7, 10, 12, 15, 18)), Seq(Seq(0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0), Seq(0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 1, 1, 1), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1), Seq(0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2), Seq(0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2), Seq(0, 1, 0, 0, 0, 1, 1, 2, 2, 2, 1, 3, 3, 3, 5), Seq(0, 1, 0, 0, 0, 1, 1, 2, 3, 2, 1, 3, 4, 4, 6), Seq(0, 0, 0, 1, 1, 2, 2, 1, 1, 2, 3, 3, 4, 5, 5), Seq(1, 1, 0, 1, 1, 2, 3, 1, 2, 1, 2, 3, 4, 5, 5), Seq(0, 0, 0, 1, 1, 2, 2, 2, 1, 1, 3, 3, 4, 5, 5), Seq(0, 0, 1, 0, 0, 1, 1, 3, 2, 3, 2, 4, 5, 5, 7), Seq(0, 0, 1, 1, 1, 3, 3, 3, 3, 3, 4, 6, 7, 8, 10), Seq(0, 1, 1, 1, 1, 3, 4, 4, 4, 4, 5, 7, 8, 10, 12), Seq(0, 1, 1, 1, 1, 3, 4, 5, 5, 5, 5, 8, 10, 12, 15), Seq(0, 1, 1, 2, 2, 5, 6, 5, 5, 5, 7, 10, 12, 15, 18)), Seq(Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1), Seq(0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1), Seq(0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2), Seq(0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2), Seq(0, 0, 1, 0, 0, 1, 1, 2, 2, 2, 1, 3, 3, 3, 5), Seq(0, 0, 1, 0, 0, 1, 1, 2, 2, 3, 1, 3, 4, 4, 6), Seq(0, 0, 0, 1, 1, 2, 2, 1, 2, 1, 3, 3, 4, 5, 5), Seq(0, 0, 0, 1, 1, 2, 2, 2, 1, 1, 3, 3, 4, 5, 5), Seq(1, 0, 1, 1, 1, 2, 3, 1, 1, 2, 2, 3, 4, 5, 5), Seq(0, 1, 0, 0, 0, 1, 1, 3, 3, 2, 2, 4, 5, 5, 7), Seq(0, 1, 0, 1, 1, 3, 3, 3, 3, 3, 4, 6, 7, 8, 10), Seq(0, 1, 1, 1, 1, 3, 4, 4, 4, 4, 5, 7, 8, 10, 12), Seq(0, 1, 1, 1, 1, 3, 4, 5, 5, 5, 5, 8, 10, 12, 15), Seq(0, 1, 1, 2, 2, 5, 6, 5, 5, 5, 7, 10, 12, 15, 18)), Seq(Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0), Seq(0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1), Seq(0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1), Seq(0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 2, 2), Seq(0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 2, 2), Seq(0, 0, 0, 1, 1, 2, 2, 1, 1, 1, 3, 3, 4, 5, 5), Seq(0, 0, 0, 1, 1, 2, 3, 2, 1, 1, 4, 4, 4, 6, 6), Seq(0, 1, 1, 0, 0, 1, 2, 3, 3, 3, 2, 4, 4, 5, 7), Seq(0, 0, 1, 0, 0, 1, 1, 3, 2, 3, 2, 4, 5, 5, 7), Seq(0, 1, 0, 0, 0, 1, 1, 3, 3, 2, 2, 4, 5, 5, 7), Seq(1, 1, 1, 1, 1, 3, 4, 2, 2, 2, 4, 4, 6, 7, 7), Seq(0, 1, 1, 1, 1, 3, 4, 4, 4, 4, 4, 7, 8, 10, 12), Seq(0, 1, 1, 1, 1, 4, 4, 4, 5, 5, 6, 8, 11, 12, 14), Seq(0, 1, 1, 2, 2, 5, 6, 5, 5, 5, 7, 10, 12, 15, 17), Seq(0, 1, 1, 2, 2, 5, 6, 7, 7, 7, 7, 12, 14, 17, 23)), Seq(Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0), Seq(0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 2, 2), Seq(0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 2, 2), Seq(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3), Seq(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3), Seq(0, 0, 0, 1, 1, 3, 2, 3, 3, 3, 3, 5, 6, 6, 8), Seq(0, 1, 1, 1, 1, 2, 3, 4, 3, 3, 4, 6, 6, 8, 10), Seq(0, 1, 1, 1, 1, 3, 4, 4, 3, 3, 4, 6, 6, 8, 10), Seq(0, 0, 1, 1, 1, 3, 3, 3, 3, 3, 4, 6, 7, 8, 10), Seq(0, 1, 0, 1, 1, 3, 3, 3, 3, 3, 4, 6, 7, 8, 10), Seq(0, 1, 1, 1, 1, 3, 4, 4, 4, 4, 4, 7, 8, 10, 12), Seq(1, 1, 1, 2, 2, 5, 6, 6, 6, 6, 7, 11, 12, 14, 18), Seq(0, 1, 1, 2, 2, 6, 6, 6, 7, 7, 8, 12, 16, 18, 22), Seq(0, 2, 2, 2, 2, 6, 8, 8, 8, 8, 10, 14, 18, 22, 27), Seq(0, 2, 2, 3, 3, 8, 10, 10, 10, 10, 12, 18, 22, 27, 32)), Seq(Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0), Seq(0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 3, 2, 2), Seq(0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 3, 2, 2), Seq(0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 4), Seq(0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 2, 2, 3, 4), Seq(0, 1, 1, 1, 1, 2, 4, 4, 3, 3, 4, 6, 6, 8, 10), Seq(0, 0, 0, 1, 1, 4, 2, 2, 4, 4, 4, 6, 10, 10, 12), Seq(0, 0, 0, 1, 1, 4, 2, 2, 4, 4, 4, 6, 11, 10, 12), Seq(0, 1, 1, 1, 1, 3, 4, 4, 4, 4, 5, 7, 8, 10, 12), Seq(0, 1, 1, 1, 1, 3, 4, 4, 4, 4, 5, 7, 8, 10, 12), Seq(0, 1, 1, 1, 1, 4, 4, 4, 5, 5, 6, 8, 11, 12, 14), Seq(0, 1, 1, 2, 2, 6, 6, 6, 7, 7, 8, 12, 16, 18, 22), Seq(1, 3, 3, 2, 2, 6, 10, 11, 8, 8, 11, 16, 16, 22, 26), Seq(0, 2, 2, 3, 3, 8, 10, 10, 10, 10, 12, 18, 22, 26, 32), Seq(0, 2, 2, 4, 4, 10, 12, 12, 12, 12, 14, 22, 26, 32, 40)), Seq(Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0), Seq(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3), Seq(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3), Seq(0, 0, 0, 1, 0, 1, 2, 1, 1, 1, 2, 2, 3, 4, 4), Seq(0, 0, 0, 0, 1, 1, 2, 1, 1, 1, 2, 2, 3, 4, 4), Seq(0, 1, 1, 1, 1, 3, 4, 3, 3, 3, 5, 6, 8, 11, 12), Seq(0, 1, 1, 2, 2, 4, 5, 4, 4, 4, 6, 8, 10, 12, 14), Seq(0, 1, 1, 1, 1, 3, 4, 5, 5, 5, 5, 8, 10, 12, 15), Seq(0, 1, 1, 1, 1, 3, 4, 5, 5, 5, 5, 8, 10, 12, 15), Seq(0, 1, 1, 1, 1, 3, 4, 5, 5, 5, 5, 8, 10, 12, 15), Seq(0, 1, 1, 2, 2, 5, 6, 5, 5, 5, 7, 10, 12, 15, 17), Seq(0, 2, 2, 2, 2, 6, 8, 8, 8, 8, 10, 14, 18, 22, 27), Seq(0, 2, 2, 3, 3, 8, 10, 10, 10, 10, 12, 18, 22, 26, 32), Seq(1, 2, 2, 4, 4, 11, 12, 12, 12, 12, 15, 22, 26, 31, 38), Seq(0, 3, 3, 4, 4, 12, 14, 15, 15, 15, 17, 27, 32, 38, 47)), Seq(Seq(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1), Seq(0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 4), Seq(0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 4), Seq(0, 1, 1, 0, 0, 1, 1, 2, 2, 2, 2, 3, 4, 4, 5), Seq(0, 1, 1, 0, 0, 1, 1, 2, 2, 2, 2, 3, 4, 4, 5), Seq(0, 1, 1, 1, 1, 3, 4, 5, 5, 5, 5, 8, 10, 12, 15), Seq(0, 1, 1, 1, 1, 4, 5, 6, 6, 6, 6, 10, 12, 14, 18), Seq(0, 1, 1, 2, 2, 5, 6, 5, 5, 5, 7, 10, 12, 15, 18), Seq(0, 1, 1, 2, 2, 5, 6, 5, 5, 5, 7, 10, 12, 15, 18), Seq(0, 1, 1, 2, 2, 5, 6, 5, 5, 5, 7, 10, 12, 15, 18), Seq(0, 1, 1, 2, 2, 5, 6, 7, 7, 7, 7, 12, 14, 17, 23), Seq(0, 2, 2, 3, 3, 8, 10, 10, 10, 10, 12, 18, 22, 27, 32), Seq(0, 2, 2, 4, 4, 10, 12, 12, 12, 12, 14, 22, 26, 32, 40), Seq(0, 3, 3, 4, 4, 12, 14, 15, 15, 15, 17, 27, 32, 38, 47), Seq(1, 4, 4, 5, 5, 15, 18, 18, 18, 18, 23, 32, 40, 47, 56))))
  }

//    "M_24" should "compute the character table without choking" in {
//    println(FiniteGroups.Mathieu24.characterTable)
//  }

}

