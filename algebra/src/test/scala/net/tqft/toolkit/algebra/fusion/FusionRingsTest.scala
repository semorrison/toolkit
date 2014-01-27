package net.tqft.toolkit.algebra.fusion

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import net.tqft.toolkit.algebra.matrices.Matrix
import net.tqft.toolkit.algebra.grouptheory.FiniteGroups

@RunWith(classOf[JUnitRunner])
class FusionRingsTest extends FlatSpec with ShouldMatchers {

  "withObject" should "correctly find all fusion rings with a given object" in {
    FusionRings.withObject(FusionRings.Examples.AH1.structureCoefficients(1) /*, Some(FusionRings.Examples.AH1)*/ ).toSeq should have size (1)
  }

	
 "representationOf" should "generate fusion rings from finite groups" in {
   FusionRings.Examples.representationsOf(FiniteGroups.cyclicGroup(6)).globalDimensionLowerBound < 6.0 should be (true)
 }
}
