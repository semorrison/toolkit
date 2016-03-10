package net.tqft.toolkit.algebra.fusion4

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
class UMTCEnumeratorTest extends FlatSpec with Matchers {

  "firstNonInvertibleObjectMatrices" should "find all the initial matrices" in {
    val rank =4
    val enumerator = UMTCEnumerator(rank, 0, 20.0)
    val matrices = enumerator.firstNonInvertibleObjectMatrices
    //    println(matrices.size)
//    for (m <- enumerator.firstNonInvertibleObjectMatricesWithEigendata; d <- m.diagonalisation; s <- d.symmetrised; N <- s.verlindeMultiplicities) {
//      val max = N.map(_.map(_.max).max).max
//      val sep = if (max >= 10) "," else ""
//      println(rank + ",0 " + max + " " + N.map(_.map(_.mkString(sep)).mkString(sep)).mkString(sep))
//      //      println(N.map(_.map(_.mkString("{",",","}")).mkString("{",",","}")).mkString("{",",","}"))
//      //      println(N.transpose.map(_.map(_.mkString).mkString(" ")).mkString("\n"))
//      //      println
//    }

    import net.tqft.toolkit.collections.Tally._
    import Ordering.Implicits._
    val eigenspaceSizes = (for (m <- enumerator.firstNonInvertibleObjectMatricesWithEigendata; pfr <- m.diagonalisationOrPartialFusionRing.right.toOption) yield {
      m.eigenspaces.map(_.eigenbasis.size).sorted
    }).tally.toSeq.sorted

    for (p <- eigenspaceSizes) println(p)

  }

}