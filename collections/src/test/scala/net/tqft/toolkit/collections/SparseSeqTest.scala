package net.tqft.toolkit.collections

import org.scalatest._

import scala.math._

class SparseSeqTest extends FlatSpec with Matchers {
  
  "toSeq" should "the original Seq" in {
    import SparseSeq._
    val seq = Seq(1,0,2,3,0)
    val sseq: SparseSeq[Int] = seq
    sseq.toSeq should equal (seq)
  }
  
}

