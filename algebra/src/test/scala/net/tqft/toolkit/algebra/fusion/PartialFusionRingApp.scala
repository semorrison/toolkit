package net.tqft.toolkit.algebra.fusion

import net.tqft.toolkit.Profiler

object PartialFusionRingApp extends App {

  val L = if(args.length > 0) {
    args(0).toDouble
  } else {
    12.0
  }
  //  while (true) {
  println("completed in " + Profiler.timing({
    val generators = FusionRings.Examples.allGenerators(L).toStream

    println("warming up... preparing a first layer")
    val firstLayer = for (s <- generators.toSeq; (r, p) <- PartialFusionRing(2, s, L).descendantsWithProgress(4 - _.ring.rank); if r.genuineReduction.flatMap(_.genuineReduction).map(_.depth).getOrElse(0) == 1) yield {
      println(p)
      r
    }

    println("parallelizing over " + firstLayer.size + " fusion rings")
    for ((s, i) <- firstLayer.zipWithIndex.par; (r, p) <- s.descendantsWithProgress()) {
      println((i + 1, firstLayer.size) + " " + p)
      if (r.depth == r.depths.max) {
        if (r.ring.associativityConstraints.forall(p => p._1 == p._2)) {
          println("Found a depth " + r.depth + " fusion ring: " + r.ring.structureCoefficients.map(_.entries))
        } else {
          //          println("   and an incomplete one with global dimension " + r.ring.globalDimensionLowerBound + ": " + r.ring.structureCoefficients.map(_.entries))
        }
      }
    }
  })._1)
  //  }

  // 2013-01-15
  // PartialFusionRing(2, FusionRings.Examples.rank2(0), 9.0).descendants()
  // 27000ms
  // 13100ms BoundedDiophantineSolver doesn't optimize choosing a branch when there are no equations left
  // 12100ms being a bit lazier finding FP eigenvalues
  // 8900ms (switching to times after warmup)
  // 8400ms trimming partialAssociativityConstraints
  // 7300ms trimming dualityConstraints
  // 6100ms optimizing constraints
  // 2013-01-16
  // 4000ms fixing a major bug; an explicit ordering on Lowers, not an invariant (faster as well as correcter!)
  // val L = 9.0; for (s <- rank2s; r <- PartialFusionRing(2, s, L).descendants())
  // 4400ms
  // 4750ms
  // 4500ms just can't seem to improve on this...

  // L = 10.0
  // 25500ms

  // L = 11.0 
  // 185s
}