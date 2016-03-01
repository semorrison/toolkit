package net.tqft.toolkit.algebra.fusion3

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import net.tqft.toolkit.algebra.grouptheory.FiniteGroups

@RunWith(classOf[JUnitRunner])
class EnumerationVerifyTest extends FlatSpec with Matchers {
   
  "Enumeration" should "accept Z/3Z as a umtc" in {
    val E = Enumeration(1,1, 3.1, umtc = true, None, None,None)
    E.verify(FiniteGroups.cyclicGroup(3).tensorProductMultiplicities) should equal(true)
  }
}