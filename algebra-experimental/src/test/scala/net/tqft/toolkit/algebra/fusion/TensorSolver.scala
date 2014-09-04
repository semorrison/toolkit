package net.tqft.toolkit.algebra.fusion

object TensorSolver extends App {
  // 2012-11-27: 666631 (Z/4) has no fusion rules, apparently.
//  val result1 = FusionBimodules.withGenerator("bwd1v1v1v1v1v1v1p1p1p1p1v1x0x0x0x0p0x1x0x0x0p0x0x1x0x0p0x0x0x0x1v1x0x0x0p0x1x0x0p0x0x1x0p0x0x0x1v1x0x0x0p0x1x0x0p0x0x1x0v1x0x0p0x1x0p0x0x1v1x0x0p0x1x0p0x0x1duals1v1v1v1v2x1x3x4v2x1x3v2x1x3", "bwd1v1v1v1v1v1v1p1p1p1p1v1x0x0x0x0p0x1x0x0x0p0x0x1x0x0p0x0x0x0x1v1x0x0x0p0x1x0x0p0x0x1x0p0x0x0x1v1x0x0x0p0x1x0x0p0x0x1x0v1x0x0p0x1x0p0x0x1v1x0x0p0x1x0p0x0x1duals1v1v1v1v2x1x3x4v2x1x3v2x1x3")
//  for (b <- result1) println(b)

  // 2012-12-12: no fusion rules for (Z/2 x Z/2) either.
  println("looking for fusion rules for 666631 (Z/2 x Z/2)")
  val result2 = FusionBimodules.withGenerator("bwd1v1v1v1v1v1v1p1p1p1p1v1x0x0x0x0p0x1x0x0x0p0x0x1x0x0p0x0x0x0x1v1x0x0x0p0x1x0x0p0x0x1x0p0x0x0x1v1x0x0x0p0x1x0x0p0x0x1x0v1x0x0p0x1x0p0x0x1v1x0x0p0x1x0p0x0x1duals1v1v1v1v2x1x3x4v2x1x3v2x1x3", "bwd1v1v1v1v1v1v1p1p1p1p1v1x0x0x0x0p0x1x0x0x0p0x0x1x0x0p0x0x0x0x1v1x0x0x0p0x1x0x0p0x0x1x0p0x0x0x1v1x0x0x0p0x1x0x0p0x0x1x0v1x0x0p0x1x0p0x0x1v1x0x0p0x1x0p0x0x1duals1v1v1v1v1x2x3x4v1x2x3v1x2x3")
  for (b <- result2) println(b)
  println("done")

}