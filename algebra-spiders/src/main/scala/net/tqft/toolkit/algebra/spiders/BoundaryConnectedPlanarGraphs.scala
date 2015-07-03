package net.tqft.toolkit.algebra.spiders

import net.tqft.toolkit.functions.Memo

object BoundaryConnectedPlanarGraphs {
  private val spider = DiagramSpider.graphSpider

  private val pentagonNextToSquare = spider.multiply(PlanarGraph.polygon(4), spider.rotate(spider.multiply(PlanarGraph.trivalentVertex, PlanarGraph.H, 1), -1), 2)
  private val IPendant = spider.multiply(spider.multiply(PlanarGraph.trivalentVertex, spider.rotate(PlanarGraph.twoSquares, -1), 2), PlanarGraph.trivalentVertex, 2)

  def apply(n: Int, k: Int, connectedGraphGenerator: (Int, Int) => Seq[PlanarGraph]): Seq[PlanarGraph] = {
    // connectedGraphGenerator(r, s) gives a Seq of PlanarGraphs with r boundary points and s internal faces
    def product(xx: Seq[Seq[PlanarGraph]]): Seq[Seq[PlanarGraph]] =
      xx match {
        case aa +: Nil =>
          aa.map(Seq(_))
        case aa +: bb +: Nil =>
          aa.map(a => bb.map(b => Seq(a, b))).flatten
        case aa +: bb +: cc =>
          product(bb +: cc).map(li => aa.map(a => a +: li)).flatten
        case Seq() +: _ =>
          Seq()
        case _ => xx
      }

    val compositions = {
      def compositions_(n: Int, s: Int): Stream[Seq[Int]] = (1 until s).foldLeft(Stream(Nil: Seq[Int])) { // Weak integer compositions of n into s cells
        (a, _) => a.flatMap(c => Stream.range(0, n - c.sum + 1).map(_ +: c))
      }.map(c => (n - c.sum) +: c)

      Memo(compositions_ _)
    }

    val cachedConnectedGraphGenerator = Memo(connectedGraphGenerator)

    PlanarPartitions(n).flatMap((partition: Seq[Seq[Int]]) => {
      //println(s"partition: $partition")

      compositions(k, partition.length).flatMap((internalFaceNumbers: Seq[Int]) => {
        assert(internalFaceNumbers.length == partition.length, "Lengths of partition and composition do not match!")
        val components = for (i <- 0 until partition.length) yield cachedConnectedGraphGenerator(partition(i).length, internalFaceNumbers(i))

        //println(s"components: $components")
        //for (x <- components) {println(x)}
        //println(s"product(components): ${product(components)}")
        //for (x <- product(components)) {println(x)}

        product(components).map((ds: Seq[PlanarGraph]) => { DiagramSpider.graphSpider.assembleAlongPlanarPartition(partition, ds) })
      })
    })

  }

  def trivalent(n: Int, k: Int) = {
    def generator(r: Int, s: Int) = if (r > 2)
      ConnectedTrivalentPlanarGraphs(r, s).filterNot(_.hasTinyFace).flatMap(
        (G: PlanarGraph) => Seq.tabulate(r)((rotation: Int) => DiagramSpider.graphSpider.rotate(G, rotation).canonicalFormWithDefect._1)).distinct
    else if (r == 2 && s == 0) Seq(PlanarGraph.strand)
    else if (r == 2 && s == 4) Seq(IPendant)
    else Seq()

    apply(n, k, generator(_, _)).filterNot(_.containsSubgraph(pentagonNextToSquare))
  }
}