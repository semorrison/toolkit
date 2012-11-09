package net.tqft.toolkit.algebra.fusion

object TensorSolver extends App {
    val result = FusionBimodules.withGenerator("bwd1v1v1v1v1v1v1p1p1p1p1v1x0x0x0x0p0x1x0x0x0p0x0x1x0x0p0x0x0x0x1v1x0x0x0p0x1x0x0p0x0x1x0p0x0x0x1v1x0x0x0p0x1x0x0p0x0x1x0v1x0x0p0x1x0p0x0x1v1x0x0p0x1x0p0x0x1duals1v1v1v1v2x1x3x4v2x1x3v2x1x3", "bwd1v1v1v1v1v1v1p1p1p1p1v1x0x0x0x0p0x1x0x0x0p0x0x1x0x0p0x0x0x0x1v1x0x0x0p0x1x0x0p0x0x1x0p0x0x0x1v1x0x0x0p0x1x0x0p0x0x1x0v1x0x0p0x1x0p0x0x1v1x0x0p0x1x0p0x0x1duals1v1v1v1v2x1x3x4v2x1x3v2x1x3")
    for(b <- result) println(b)
}