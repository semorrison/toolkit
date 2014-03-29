package net.tqft.toolkit.algebra

trait Rig[@specialized(Int, Long, Float, Double) A] extends Monoid[A] with AdditiveMonoid[A] {
  def multiplyByInt(x: A, y: Int): A = multiply(x, fromInt(y))
  def fromInt(x: Int): A = {
    require(x >= 0)
    if (x == 0) {
      zero
    } else {
      Iterator.fill(x)(one).reduce(add)
    }
  }
}

trait RigLowPriorityImplicits {
  implicit def pointwiseRigMap[A, B: Rig]: Rig[Map[A, B]] = new Rig.PointwiseRigMap[A, B]
}

object Rig extends RigLowPriorityImplicits {
  implicit def forget[A: Ring]: Rig[A] = implicitly[Ring[A]]

  protected trait RigMapLike[A, B] extends ModuleOverRig[B, Map[A, B]] {
    def coefficients: Rig[B]

    override def scalarMultiply(b: B, m: Map[A, B]) = m.mapValues(v => coefficients.multiply(b, v))
  }

  class RigMap[A: AdditiveMonoid, B: Rig] extends AdditiveMonoid.AdditiveMonoidMap[A, B] with Rig[Map[A, B]] with RigMapLike[A, B] {
    def keys = implicitly[AdditiveMonoid[A]]
    override def coefficients = implicitly[Rig[B]]

    override def one = Map(keys.zero -> coefficients.one)
    override def multiply(m1: Map[A, B], m2: Map[A, B]): Map[A, B] = {
      val newMap = scala.collection.mutable.Map[A, B]().withDefault(_ => coefficients.zero)
      for ((a1, b1) <- m1; (a2, b2) <- m2) {
        val p = keys.add(a1, a2)
        newMap(p) = coefficients.add(newMap(p), coefficients.multiply(b1, b2))
      }
      Map() ++ newMap
    }
  }

  class PointwiseRigMap[A, B: Rig] extends AdditiveMonoid.AdditiveMonoidMap[A, B] with Rig[Map[A, B]] with RigMapLike[A, B] {
    override def coefficients = implicitly[Rig[B]]
    override def one = Map().withDefault(_ => coefficients.one)
    override def multiply(m1: Map[A, B], m2: Map[A, B]): Map[A, B] = {
      for ((a, b) <- m1; c = m2.get(a).getOrElse(coefficients.zero)) yield (a -> coefficients.multiply(b, c))
    }
  }

  class RigSeq[B: Rig] extends AdditiveMonoid.AdditiveMonoidSeq[B] with Rig[Seq[B]] {
    override def coefficients = implicitly[Rig[B]]

    override def one = Seq(coefficients.one)
    override def multiply(s1: Seq[B], s2: Seq[B]): Seq[B] = {
      ???
    }
  }

  implicit def rigMap[A: AdditiveMonoid, B: Rig]: Rig[Map[A, B]] = new RigMap[A, B]
  implicit def rigSeq[B: Rig]: Rig[Seq[B]] = new RigSeq[B]

}

trait CommutativeRig[A] extends Rig[A]