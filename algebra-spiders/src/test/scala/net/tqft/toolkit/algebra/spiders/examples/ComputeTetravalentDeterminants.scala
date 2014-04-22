package net.tqft.toolkit.algebra.spiders.examples

import net.tqft.toolkit.algebra._
import net.tqft.toolkit.algebra.spiders._
import net.tqft.toolkit.algebra.polynomials._
import net.tqft.toolkit.algebra.matrices.Matrix
import net.tqft.toolkit.algebra.mathematica._

object ComputeTetravalentDeterminants extends App {
    {
      lazy val diagrams6 = TetravalentSpider.reducedDiagrams(6, 0) ++ TetravalentSpider.reducedDiagrams(6, 1) ++ TetravalentSpider.reducedDiagrams(6, 2) ++ TetravalentSpider.reducedDiagrams(6, 3)
  
      require(diagrams6.size == 16)
  
      val m0 = Matrix(14, TetravalentSpider.innerProductMatrix(diagrams6.dropRight(2), diagrams6.dropRight(2)).map(_.map(x => (x: MultivariableRationalFunction[BigInt, String]))))
      val m1 = Matrix(15, TetravalentSpider.innerProductMatrix(diagrams6.dropRight(1), diagrams6.dropRight(1)).map(_.map(x => (x: MultivariableRationalFunction[BigInt, String]))))
      val m2 = Matrix(16, TetravalentSpider.innerProductMatrix(diagrams6, diagrams6).map(_.map(x => (x: MultivariableRationalFunction[BigInt, String]))))
  
      import MathematicaForm._
  
      println("matrix: ")
      println(m2.entries.toIndexedSeq.toMathemathicaInputString)
      println("determinants: ")
      println(m0.determinant.toMathemathicaInputString)
      println(m1.determinant.toMathemathicaInputString)
      println(m2.determinant.toMathemathicaInputString)
    }

  {
    def diagrams8(k: Int) = for (i <- 0 to k; d <- TetravalentSpider.reducedDiagrams(8, i)) yield d

    def matrix8(k: Int, m: Int) = Matrix(diagrams8(k).size - m, TetravalentSpider.innerProductMatrix(diagrams8(k).dropRight(m), diagrams8(k).dropRight(m)).map(_.map(x => (x: MultivariableRationalFunction[BigInt, String]))))

    import MathematicaForm._

    for (k <- 0 to 3) {
      println(diagrams8(k).size + s" diagrams with at most $k vertices")
      val m0 = matrix8(k, 0)
      println(s"matrix (up to $k vertices): ")
      println(m0.entries.toIndexedSeq.toMathemathicaInputString)
      println(s"determinant: ")
      println(m0.determinant.toMathemathicaInputString)
    }
    for (m <- 1 to 0 by -1) {
      val m0 = matrix8(4, m)
      println(s"matrix (up to 4 vertices, dropping $m): ")
      println(m0.entries.toIndexedSeq.toMathemathicaInputString)
      println(s"determinant: ")
      println(m0.determinant.toMathemathicaInputString)
    }

  }
}