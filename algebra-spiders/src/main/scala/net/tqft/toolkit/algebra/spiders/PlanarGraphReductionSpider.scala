package net.tqft.toolkit.algebra.spiders

import net.tqft.toolkit.algebra._

abstract class PlanarGraphReductionSpider[R: Ring] extends SubstitutionSpider.PlanarGraphMapSubstitutionSpider[R] with ReductionSpider[PlanarGraph, R] {
  // move these further up the hierarchy?

  def innerProductMatrix(diagrams1: Seq[PlanarGraph], diagrams2: Seq[PlanarGraph]): Seq[Seq[R]] = {
    def ring = implicitly[Ring[R]]

    for (r <- reductions) println(r)

    (for (x <- diagrams1) yield {
      println("computing inner products: x = " + x)
      (for (y <- diagrams2) yield {
        println("computing inner products: y = " + y)
        println("Reductions: ")
        for (r <- reductions) println("  " + r)
        evaluatedInnerProduct(Map(x -> ring.one), Map(y -> ring.one))
      })
    })
  }
  def innerProductMatrix(diagrams: Seq[PlanarGraph]): Seq[Seq[R]] = innerProductMatrix(diagrams, diagrams)

  def reducedDiagrams(numberOfBoundaryPoints: Int, numberOfVertices: Int): Seq[PlanarGraph] = {
    require(vertexTypes.size == 1)
    reducedDiagrams(numberOfBoundaryPoints, Map(vertexTypes.head -> numberOfVertices))
  }
  def reducedDiagrams(numberOfBoundaryPoints: Int, numberOfVertices: Map[VertexType, Int]): Seq[PlanarGraph] = graphs.avoiding(reductions.map(_.big)).byNumberOfVertices(numberOfBoundaryPoints, numberOfVertices)
}
