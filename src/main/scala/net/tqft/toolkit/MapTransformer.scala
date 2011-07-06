package net.tqft.toolkit

object MapTransformer {

  implicit def valuesTransformable[A, B](map: scala.collection.mutable.Map[A, B]) = new ValuesTransformable(map)
  
  class ValuesTransformable[A, B](map: scala.collection.mutable.Map[A, B]) {
    def transformValues[C](f1: B => C, f2: C => B): scala.collection.mutable.Map[A, C] =  new ValueTransformer(map, f1, f2)
    def transformKeys[Z](f1: A => Z, f2: Z => A): scala.collection.mutable.Map[Z, B] =  new KeyTransformer(map, f1, f2)
  }
  
  class ValueTransformer[A, B, C](val map: scala.collection.mutable.Map[A, B], f1: B => C, f2: C => B) extends scala.collection.mutable.Map[A, C] {
    override def get(key: A): Option[C] = {
      map.get(key) map { f1(_) }
    }

    override def iterator: Iterator[(A, C)] = {
      map.iterator map { case (a, b) => (a, f1(b)) }
    }

    override def +=(kv: (A, C)) = {
      kv match {
        case (key, value) => {
          map += ((key, f2(value)))
          this
        }
      }
    }
    override def -=(key: A) = {
      map -= key
      this
    }

  }
  private class KeyTransformer[Z, A, B](val map: scala.collection.mutable.Map[A, B], f1: A => Z, f2: Z => A) extends scala.collection.mutable.Map[Z, B] {
    override def get(key: Z): Option[B] = {
      map.get(f2(key))
    }

    override def iterator: Iterator[(Z, B)] = {
      map.iterator map { case (a, b) => (f1(a), b) }
    }

    override def +=(kv: (Z, B)) = {
      kv match {
        case (key, value) => {
          map += ((f2(key), value))
          this
        }
      }
    }
    override def -=(key: Z) = {
      map -= f2(key)
      this
    }

  }
}