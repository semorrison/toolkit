package net.tqft.toolkit.algebra.khovanov

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._

@RunWith(classOf[JUnitRunner])
class MovieMoveTest extends FlatSpec with Matchers {
    "movie moves" should "have homotopic chain maps" in {
      for((left, right) <- MovieMoves.all) {
        // FIXME, actually we just want the maps to be homotopic
        KhovanovHomology.applyToMorphism(left) should equal(KhovanovHomology.applyToMorphism(right))
      }
    }
}
