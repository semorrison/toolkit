package net.tqft.toolkit.collections

object Rotate {
  implicit def rotatable[A](i: List[A]) = new Rotatable(i)

  class Rotatable[A](i: List[A]) {
    def rotateLeft(k: Int) = {
      if (i.size == 0) {
        i
      } else {
        val m = k % i.size
        i.drop(m) ::: i.take(m)
      }
    }
    def rotateRight(k: Int) = {
      if (i.size == 0) {
        i
      } else {
        val m = k % i.size
        i.takeRight(m) ::: i.dropRight(m)
      }
    }
  }

}