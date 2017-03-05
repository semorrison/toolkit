package net.tqft.toolkit.algebra.spiders

import net.tqft.toolkit.algebra.enumeration.CanonicalGeneration
import net.tqft.toolkit.algebra.grouptheory.FinitelyGeneratedFiniteGroup
import net.tqft.toolkit.algebra.grouptheory.FiniteGroups
import net.tqft.toolkit.algebra.grouptheory.FinitelyGeneratedFiniteGroup
import net.tqft.toolkit.algebra.graphs.Dreadnaut

case class PlanarGraphEnumerationContext(vertices: Seq[VertexType]) {
  val spider = PlanarGraph.spider

  case class PlanarGraphEnumeration(G: PlanarGraph) extends CanonicalGeneration[PlanarGraphEnumeration, Unit] { pge =>
    val newLabel = (0 +: G.labels.map(_._1)).max + 1

    override lazy val automorphisms: FinitelyGeneratedFiniteGroup[Unit] = FinitelyGeneratedFiniteGroup.trivialGroup(())

    case class Upper(whereToStart: Int, vertexToAdd: VertexType, vertexRotation: Int, numberOfStitches: Int, basepointOffset: Option[Int]) {
      lazy val result = {
        val vertex = PlanarGraph.star(vertexToAdd)
        PlanarGraphEnumeration(
          spider.rotate(
            spider.multiply(spider.rotate(G, whereToStart), spider.rotate(vertex, vertexRotation), numberOfStitches),
            basepointOffset match {
              case None => G.numberOfBoundaryPoints - whereToStart
              case Some(r) => -r
            }))
      }
      def inverse = result.Lower(if (basepointOffset.isEmpty) G.numberOfBoundaryPoints - whereToStart else 0, G.numberOfInternalVertices + 1)
    }
    case class Lower(boundaryInterval: Int, vertexToRemove: Int) {
      require(vertexToRemove != 0)
      
      private  lazy val relabeled = G.copy(labels = G.labels.updated(vertexToRemove - 1, (newLabel, G.labels(vertexToRemove - 1)._2)))

      lazy val result = {
        val rotated = spider.rotate(relabeled, boundaryInterval /* negative?! */ )
        
        DrawPlanarGraph.showPDF(rotated)
        
        val excisions = rotated.Subgraphs(PlanarGraph.star(G.vertexFlags(vertexToRemove).size, newLabel, G.labels(vertexToRemove - 1)._2)).excisions
        
        val result = excisions.next
        require(result.depth == 0)
        require(!excisions.hasNext)
        
        PlanarGraphEnumeration(result.cut)
      }

      def encodeAsPlanarGraph: PlanarGraph = {
        val markerVertex = PlanarGraph.star(VertexType(1, 1, 1))
        spider.rotate(spider.tensor(markerVertex, spider.rotate(relabeled, boundaryInterval)), -boundaryInterval).relabelEdgesAndFaces
      }
    }

    override def upperObjects: automorphisms.ActionOnFiniteSet[Upper] = new automorphisms.ActionOnFiniteSet[Upper] {
      override def elements = {
        val elementsThatDontCoverBasepoint =
          for (
            vertexToAdd <- vertices;
            numberOfStitches <- 1 to scala.math.min(vertexToAdd.perimeter, G.numberOfBoundaryPoints);
            whereToStart <- numberOfStitches to G.numberOfBoundaryPoints;
            vertexRotation <- 0 until vertexToAdd.allowedRotationStep
          ) yield {
            Upper(whereToStart, vertexToAdd, vertexRotation, numberOfStitches, None)
          }
        val elementsThatDoCoverBasepoint =
          for (
            vertexToAdd <- vertices;
            numberOfStitches <- 2 to scala.math.min(vertexToAdd.perimeter, G.numberOfBoundaryPoints);
            whereToStart <- 1 until numberOfStitches;
            vertexRotation <- 0 until vertexToAdd.allowedRotationStep;
            basepointOffset <- 0 to (vertexToAdd.perimeter - numberOfStitches - (if (numberOfStitches == G.numberOfBoundaryPoints) 1 else 0))
          ) yield {
            Upper(whereToStart, vertexToAdd, vertexRotation, numberOfStitches, Some(basepointOffset))
          }
        val results = elementsThatDoCoverBasepoint ++ elementsThatDontCoverBasepoint
        for (r <- results) {
          println(r)
        }

        results
      }
      override def act(g: Unit, upper: Upper) = upper
    }
    override lazy val lowerObjects = new automorphisms.ActionOnFiniteSet[Lower] {
      override val elements = for (i <- 0 until G.numberOfBoundaryPoints; j <- G.allVerticesAdjacentToFace(G.boundaryFaces(i)); if j != 0) yield {
        Lower(i, j)
      }
      override def act(g: Unit, lower: Lower) = lower
    }

    override val ordering: Ordering[lowerObjects.Orbit] = {
      import Ordering.Implicits._
      Ordering.by({ o: lowerObjects.Orbit =>
        // TODO automate dangliness!
        Dreadnaut.canonicalLabelling(o.representative.encodeAsPlanarGraph.nautyGraph)
      })
    }

  }

}
