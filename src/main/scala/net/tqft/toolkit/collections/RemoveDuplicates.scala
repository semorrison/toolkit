package net.tqft.toolkit.collections
import net.tqft.toolkit.Logging

object RemoveDuplicates {
  implicit def iterable2RemoveDuplicates[A](xs: Iterable[A]) = new RemoveDuplicatesable(xs)

  class RemoveDuplicatesable[A](xs: Iterable[A]) {
//    def removeDuplicates(f: (A, A) => Boolean = { (p: A, q: A) => p == q }): List[A] = {
//      def addToListIfNew(list: List[A], x: A) = {
//        if (list.contains({ y: A => f(x, y) })) list else x :: list
//      }
//      xs.foldLeft(List[A]())(addToListIfNew(_, _))
//    }

    def removeDuplicates(f: (A, A) => Boolean = { (p: A, q: A) => p == q }): Iterable[A] = {
      xs.filter {
        var set = scala.collection.mutable.Set[A]()
        a => {
          if(set.exists(f(a, _))) {
            false
          } else {
            set += a
            true
          }
        }
      }
    }

    def removeDuplicatesAndSort(f: (A, A) => Boolean, ordering: Ordering[A]): List[A] = {
      def addToListIfNew(list: List[A], x: A) = {
        list.find(f(x, _)) match {
          case Some(y) =>
            if (ordering.compare(x, y) < 0) {
              x :: list.filterNot(_ == y)
            } else {
              list
            }
          case None => {
            Logging.info(".accumulated " + (list.size + 1) + " unique elements")
            x :: list
          }
        }
      }
      xs.foldLeft(List[A]())(addToListIfNew(_, _))
    }
  }
}