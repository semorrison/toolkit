package net.tqft.toolkit.collections

import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.math._

@RunWith(classOf[JUnitRunner])
class GroupByTest extends FlatSpec with Matchers {

  "GroupBy" should "work correctly" in {
    import GroupBy._
    List("aa", "bcd", "ac", "bbb", "ccc").equivalenceClasses(
      invariant = { x: String => x.size },
      equivalence = { (x: String, y: String) => (x.size == y.size && x(0) == y(0)) }) should equal(List(List("ac", "aa"), List("ccc"), List("bbb", "bcd")))
  }

}

