package net.tqft.toolkit.algebra.graphs

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._

@RunWith(classOf[JUnitRunner])
class PlanarGraphTest extends FlatSpec with Matchers {
  "PlanarGraph" should "do some computations" in {

    val spider = implicitly[Spider[PlanarGraph]]
    
    spider.canonicalForm(PlanarGraph.loop) should equal(spider.canonicalForm(spider.stitch(PlanarGraph.strand)))
  }
}