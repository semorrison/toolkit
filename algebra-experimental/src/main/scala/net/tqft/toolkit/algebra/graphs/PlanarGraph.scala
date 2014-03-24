package net.tqft.toolkit.algebra.graphs

import scala.collection.mutable.ListBuffer
import net.tqft.toolkit.algebra.Ring
import net.tqft.toolkit.algebra.polynomials.Polynomial
import net.tqft.toolkit.algebra.polynomials.RationalFunction
import net.tqft.toolkit.algebra.polynomials.MultivariableRationalFunction
import net.tqft.toolkit.algebra.Fraction
import net.tqft.toolkit.algebra.Field
import net.tqft.toolkit.algebra.polynomials.RationalFunction
import net.tqft.toolkit.algebra.Module

case class PlanarGraph(vertexFlags: IndexedSeq[Seq[(Int, Int)]], loops: Int) {

  def numberOfBoundaryPoints = vertexFlags(0).size
  def outerFace = vertexFlags(0).last._2

  val numberOfVertices = vertexFlags.size
  def vertices = 0 until numberOfVertices
  lazy val edgeSet = vertexFlags.flatMap(_.map(_._1)).sorted.distinct
  lazy val faceSet = vertexFlags.flatMap(_.map(_._2)).sorted.distinct

  def edgesAdjacentTo(vertex: Int): Seq[Int] = vertexFlags(vertex).map(_._1)

  lazy val edgeIncidences: Map[Int, List[Int]] = {
    val map = scala.collection.mutable.Map[Int, ListBuffer[Int]]()
    for (v <- vertices; e <- edgesAdjacentTo(v)) {
      map.getOrElseUpdate(e, ListBuffer[Int]()) += v
    }
    Map() ++ map.mapValues(_.toList)
  }
  lazy val maxEdgeLabel = edgeIncidences.keysIterator.max
  lazy val maxFaceLabel = vertexFlags.iterator.flatMap(_.map(_._2)).max

  def target(source: Int, edge: Int): Int = {
    val ends = edgeIncidences(edge)
    if (ends.head == source) {
      ends(1)
    } else {
      ends.head
    }
  }
  def faceOnLeft(source: Int, edge: Int): Int = vertexFlags(source).find(_._1 == edge).get._2
  def faceOnRight(source: Int, edge: Int): Int = vertexFlags(target(source, edge)).find(_._1 == edge).get._2

  def boundaryFaces = vertexFlags(0).map(_._2)

  def relabelEdgesAndFaces: PlanarGraph = {
    val edgeMap = (for ((e, ei) <- edgeSet.zipWithIndex) yield (e, numberOfVertices + ei)).toMap
    val faceMap = (for ((f, fi) <- faceSet.zipWithIndex) yield (f, numberOfVertices + edgeMap.size + fi)).toMap

    PlanarGraph(vertexFlags.map(_.map(p => (edgeMap(p._1), faceMap(p._2)))), loops)
  }

  def nautyGraph: ColouredGraph[Int] = {
    require(edgeSet == (numberOfVertices until numberOfVertices + edgeSet.size))
    require(faceSet == (numberOfVertices + edgeSet.size  until numberOfVertices + edgeSet.size + faceSet.size))
    
    val edgeToFaceAdjacencies = IndexedSeq.fill(edgeSet.size)(ListBuffer[Int]())

    val vertexToEdgeAdjacencies = for (flags <- vertexFlags) yield {
      flags.map(flag => {
        edgeToFaceAdjacencies(flag._1 - numberOfVertices) += flag._2
        flag._1
      }) ++ flags.headOption.map(_._2) // We add an extra edge from each vertex to each starred face
    }
        
    ColouredGraph(numberOfVertices + edgeSet.size + faceSet.size,
      vertexToEdgeAdjacencies ++ edgeToFaceAdjacencies,
      (0 +: IndexedSeq.fill(numberOfVertices - 1)(1)) ++ IndexedSeq.fill(edgeSet.size)(2) ++ IndexedSeq.fill(faceSet.size)(3))
  }

  case class Subgraphs(shape: PlanarGraph) {
    case class Excision(cut: PlanarGraph, depth: Int, rotations: Map[Int, Map[Int, Int]]) {
      def replace(other: PlanarGraph): PlanarGraph = ???
    }

    def excisions: Iterator[Excision] = ???
  }
}

object PlanarGraph {
  private def spider = implicitly[Spider[PlanarGraph]]

  def empty = polygon(0)

  def loop = {
    PlanarGraph(IndexedSeq(IndexedSeq.empty), 1)
  }

  def strand = {
    PlanarGraph(IndexedSeq(IndexedSeq((0, 1), (0, 0))), 0)
  }

  def polygon(k: Int) = {
    val flags = IndexedSeq.tabulate(k)(i => (i, i)) +:
      IndexedSeq.tabulate(k)(i => IndexedSeq(???, ???, ???))
    PlanarGraph(flags, 0)
  }

  def star(k: Int) = {
    import net.tqft.toolkit.arithmetic.Mod._

    val flags = IndexedSeq(
      Seq.tabulate(k)(i => (i, i + 1 mod k)),
      Seq.tabulate(k)(i => (i, i)))
    PlanarGraph(flags, 0)
  }

  def I = spider.multiply(star(3), star(3), 1)
}

trait Spider[A] {
  def rotate(a: A, k: Int): A
  def tensor(a1: A, a2: A): A
  def stitch(a: A): A
  def canonicalForm(a: A): A
  def circumference(a: A): Int

  def stitchAt(a: A, k: Int): A = rotate(stitch(rotate(a, 1 - k)), k - 1)
  def stitchesAt(a: A, k: Int, m: Int): A = (k until k - m by -1).foldLeft(a)(stitchAt)

  def multiply(a1: A, a2: A, m: Int): A = {
    stitchesAt(tensor(a1, a2), circumference(a1), m)
  }

  def innerProduct(a1: A, a2: A): A = {
    multiply(a1, a2, circumference(a1))
  }
  def normSquared(a: A): A = {
    innerProduct(a, a)
  }
}

case class Disk[C](circumference: Int, contents: C)

object Spider {
  implicit def graphSpider: Spider[PlanarGraph] = {
    new Spider[PlanarGraph] {
      override def circumference(graph: PlanarGraph) = graph.numberOfBoundaryPoints
      override def rotate(graph: PlanarGraph, k: Int) = {
        import net.tqft.toolkit.collections.Rotate._
        PlanarGraph(graph.vertexFlags.updated(0, graph.vertexFlags(0).rotateLeft(k)), graph.loops)
      }
      override def tensor(graph1: PlanarGraph, graph2: PlanarGraph) = {
        def flags = {
          val ne = graph1.maxEdgeLabel
          val nf = graph1.maxFaceLabel
          val relabelFlag: ((Int, Int)) => (Int, Int) = {
            case (e, f) if f == graph2.outerFace => (e + 1 + ne, graph1.outerFace)
            case (e, f) => (e + 1 + ne, f + 1 + nf)
          }
          val externalFlag = {
            graph1.vertexFlags.head ++ graph2.vertexFlags.head.map(relabelFlag)
          }
          (externalFlag +: graph1.vertexFlags.tail) ++ graph2.vertexFlags.tail.map(_.map(relabelFlag))
        }
        PlanarGraph(flags, graph1.loops + graph2.loops)
      }
      override def stitch(graph: PlanarGraph) = {
        require(graph.numberOfBoundaryPoints >= 2)

        val f1 = graph.vertexFlags(0)(1)._2
        def relabelFace(f: Int) = {
          if (f == f1) {
            graph.outerFace
          } else {
            f
          }
        }
        val e0 = graph.vertexFlags(0)(0)._1
        val e1 = graph.vertexFlags(0)(1)._1

        if (e0 == e1) {
          PlanarGraph(graph.vertexFlags.head.drop(2) +: graph.vertexFlags.tail, graph.loops + 1)
        } else {
          val emin = if (e0 < e1) e0 else e1
          
          def flags = (graph.vertexFlags.head.drop(2) +: graph.vertexFlags.tail).map(_.map({
            case (e, f) if e == e0 || e == e1 => (emin, f)
            case (e, f) => (e, relabelFace(f))
          }))

          PlanarGraph(flags, graph.loops)
        }
      }

      override def canonicalForm(graph: PlanarGraph) = {
//        println("graph: " + graph)
        
        val packed = graph.relabelEdgesAndFaces

//        println("packed: " + packed)
        
        val nautyGraph = packed.nautyGraph
        
//        println("nauty: " + nautyGraph)
        val labelling = Dreadnaut.canonicalLabelling(nautyGraph)
        
//        println("labelling: " + labelling)
        import net.tqft.toolkit.permutations.Permutations._
        val inv = labelling.inverse
        val result = PlanarGraph(labelling.take(packed.numberOfVertices).permute(packed.vertexFlags.map(_.map(p => (inv(p._1), inv(p._2))))), graph.loops)
//        println("result: " + result)
        result
      }
    }
  }

  implicit class DiskSpider[A](spider: Spider[A]) extends Spider[Disk[A]] {
    override def rotate(disk: Disk[A], k: Int) = Disk(disk.circumference, spider.rotate(disk.contents, k))
    override def tensor(disk1: Disk[A], disk2: Disk[A]) = Disk(disk1.circumference + disk2.circumference, spider.tensor(disk1.contents, disk2.contents))
    override def stitch(disk: Disk[A]) = Disk(disk.circumference - 2, spider.stitch(disk.contents))
    override def canonicalForm(disk: Disk[A]) = Disk(disk.circumference, spider.canonicalForm(disk.contents))
    override def circumference(disk: Disk[A]) = disk.circumference
  }

}

trait LinearSpider[R, M] extends Spider[M] with Module[R, M] {
  def ring: Ring[R]
}

object LinearSpider {
  def rationalSpider = implicitly[LinearSpider[RationalFunction[Int], Disk[Map[PlanarGraph, RationalFunction[Int]]]]]

  class MapLinearSpider[A: Spider, R: Ring] extends LinearSpider[R, Map[A, R]] {
    val diagramSpider = implicitly[Spider[A]]
    override val ring = implicitly[Ring[R]]

    private def mapKeys(f: A => A)(map: TraversableOnce[(A, R)]) = {
      val newMap = scala.collection.mutable.Map[A, R]()
      for ((a, r) <- map) {
        val s = f(a)
        newMap(s) = newMap.get(s).map(v => ring.add(v, r)).getOrElse(r)
      }
      Map() ++ newMap.filter(_._2 != ring.zero)
    }

    override def rotate(map: Map[A, R], k: Int) = map.map(p => (diagramSpider.rotate(p._1, k), p._2))
    override def tensor(map1: Map[A, R], map2: Map[A, R]) = {
      val newMap = scala.collection.mutable.Map[A, R]()
      for ((a, r) <- map1; (b, s) <- map2) {
        val t = diagramSpider.tensor(a, b)
        val p = ring.multiply(r, s)
        newMap(t) = newMap.get(t).map(v => ring.add(v, p)).getOrElse(p)
      }
      Map() ++ newMap.filter(_._2 != ring.zero)
    }
    override def stitch(map: Map[A, R]) = mapKeys(diagramSpider.stitch)(map)
    override def canonicalForm(map: Map[A, R]) = mapKeys(diagramSpider.canonicalForm)(map)
    override def circumference(map: Map[A, R]) = diagramSpider.circumference(map.head._1)

    override def zero = Map.empty
    override def add(map1: Map[A, R], map2: Map[A, R]) = mapKeys(x => x)(map1.iterator ++ map2.iterator)
    override def scalarMultiply(r: R, map: Map[A, R]) = map.mapValues(v => ring.multiply(r, v)).filter(_._2 != ring.zero)
    override def negate(map: Map[A, R]) = map.mapValues(v => ring.negate(v))
  }

  implicit def lift[A: Spider, R: Ring]: LinearSpider[R, Map[A, R]] = new MapLinearSpider[A, R]

  implicit def diskLinearSpider[A, R, M](implicit spider: LinearSpider[R, M]): LinearSpider[R, Disk[M]] = new Spider.DiskSpider(spider) with LinearSpider[R, Disk[M]] {
    override def ring = spider.ring
    override def zero = ???
    override def add(disk1: Disk[M], disk2: Disk[M]) = Disk(disk1.circumference, spider.add(disk1.contents, disk2.contents))
    override def scalarMultiply(r: R, disk: Disk[M]) = Disk(disk.circumference, spider.scalarMultiply(r, disk.contents))
    override def negate(disk: Disk[M]) = Disk(disk.circumference, spider.negate(disk.contents))
  }
}

case class Reduction[A, R](big: A, small: Map[A, R])

trait SubstitutionSpider[A, R] extends LinearSpider.MapLinearSpider[A, R] {
  def eigenvalue(label: Int): R
  def rotationAllowed_?(label: Int, rotation: Int): Boolean
  def rotationsAllowed_?(rotations: Map[Int, Map[Int, Int]]) = {
    rotations.forall({ case (label, rotations) => rotations.keySet.forall(rotation => rotationAllowed_?(label, rotation)) })
  }

  def allReplacements(reduction: Reduction[A, R])(diagram: A): Iterator[Map[A, R]]
  def replace(reduction: Reduction[A, R])(element: Map[A, R]): Map[A, R] = {
    val newMap = scala.collection.mutable.Map[A, R]()
    for ((a, r) <- element) {
      import net.tqft.toolkit.collections.Iterators._
      val m: Map[A, R] = allReplacements(reduction)(a).headOption.getOrElse(Map(a -> ring.one))
      for ((b, t) <- m) {
        val p = ring.multiply(r, t)
        newMap(b) = newMap.get(b).map(v => ring.add(v, p)).getOrElse(p)
      }
    }
    Map() ++ newMap.filter(_._2 != ring.zero)
  }

  def replace(reductions: Seq[Reduction[A, R]])(element: Map[A, R]): Map[A, R] = {
    reductions.iterator.map(r => replace(r)(element)).find(_ != element).getOrElse(element)
  }
  def replaceRepeatedly(reductions: Seq[Reduction[A, R]])(element: Map[A, R]) = {
    import net.tqft.toolkit.functions.FixedPoint._
    (replace(reductions) _).fixedPoint(element)
  }
}

object SubstitutionSpider {
  abstract class PlanarGraphMapSubstitutionSpider[R: Ring] extends LinearSpider.MapLinearSpider[PlanarGraph, R] with SubstitutionSpider[PlanarGraph, R] {
    override def allReplacements(reduction: Reduction[PlanarGraph, R])(diagram: PlanarGraph) = {
      for (
        excision <- diagram.Subgraphs(reduction.big).excisions;
        if rotationsAllowed_?(excision.rotations)
      ) yield {
        val eigenvalueFactor = ring.product(excision.rotations.map({
          case (label, rotations) => {
            val totalRotation = rotations.map(p => p._1 * p._2).sum
            ring.power(eigenvalue(label), totalRotation)
          }
        }))
        val newMap = scala.collection.mutable.Map[PlanarGraph, R]()
        for ((a, r) <- reduction.small) {
          val b = diagramSpider.canonicalForm(excision.replace(a))
          val p = ring.multiply(eigenvalueFactor, r)
          newMap(b) = newMap.get(b).map(v => ring.add(v, p)).getOrElse(p)
        }
        Map() ++ newMap.filter(_._2 != ring.zero)
      }
    }
  }
}

trait ReductionSpider[A, R] extends SubstitutionSpider[A, R] {
  def reductions: Seq[Reduction[A, R]]
  override def canonicalForm(m: Map[A, R]) = super.canonicalForm(replaceRepeatedly(reductions)(m))
}

abstract class PlanarGraphReductionSpider[R: Ring] extends SubstitutionSpider.PlanarGraphMapSubstitutionSpider[R] with ReductionSpider[PlanarGraph, R]

abstract class TrivalentSpider[R: Ring] extends PlanarGraphReductionSpider[R] {
  def d: R
  def b: R
  def t: R
  def omega: R
  override def eigenvalue(label: Int) = {
    label match {
      case -1 => ???
      case 2 => ring.one
      case 3 => omega
    }
  }
  override def rotationAllowed_?(label: Int, rotation: Int) = true

  private val loopReduction = Reduction(PlanarGraph.loop, Map(PlanarGraph.empty -> d))
  private val bigonReduction = Reduction(PlanarGraph.polygon(2), Map(PlanarGraph.strand -> b))
  private val triangleReduction = Reduction(PlanarGraph.polygon(3), Map(PlanarGraph.star(3) -> t))
  override def reductions = Seq(loopReduction, bigonReduction, triangleReduction)
}

abstract class CubicSpider[R: Ring] extends TrivalentSpider[R] {
  override val omega = ring.one

  private val squareReduction = ???
  override def reductions = super.reductions :+ squareReduction
}

object `SO(3)_q` extends CubicSpider[RationalFunction[Int]] {
  override val ring = implicitly[Field[RationalFunction[Int]]]

  val q: RationalFunction[Int] = Polynomial(1 -> Fraction(1, 1))
  override val d = ring.add(q, ring.inverse(q))
  override val b = ???
  override val t = ???

  private val pentagonReduction = ???
  override def reductions = super.reductions :+ pentagonReduction
}

object `(G_2)_q` extends CubicSpider[RationalFunction[Int]] {
  override val ring = implicitly[Field[RationalFunction[Int]]]

  val q: RationalFunction[Int] = Polynomial(1 -> Fraction(1, 1))
  override val d = ???
  override val b = ???
  override val t = ???

  private val pentagonReduction = ???
  override def reductions = super.reductions :+ pentagonReduction
}

sealed trait QuantumExceptionalVariable
case object v extends QuantumExceptionalVariable
case object w extends QuantumExceptionalVariable
object QuantumExceptionalVariable {
  implicit val ordering: Ordering[QuantumExceptionalVariable] = Ordering.by({
    case `v` => 1
    case `w` => 2
  })
}

object QuantumExceptional extends TrivalentSpider[MultivariableRationalFunction[Int, QuantumExceptionalVariable]] {
  override val ring = implicitly[Field[MultivariableRationalFunction[Int, QuantumExceptionalVariable]]]

  override val omega = ring.one
  override val d = ???
  override val b = ???
  override val t = ???
  override def reductions = super.reductions
}
