package net.tqft.toolkit.functions
import net.tqft.toolkit.Logging

object Memo extends Logging {

  implicit def function2Memoable[A, B](f: A => B): Memoable[A, B] = new Memoable(f)

  class Memoable[A, B](f: A => B) {
    def memo = Memo(f)
    def memoSoftly = Memo.softly(f)
    def memoUsing(cache: scala.collection.mutable.Map[A, B]) = Memo(f, cache)
  }

  def apply[A, B](f: A => B): A => B = {
    import scala.collection.JavaConversions._
    apply(f, new com.google.common.collect.MapMaker().makeMap[A, B]())
  }

  def softly[A, B](f: A => B): A => B = {
    import scala.collection.JavaConversions._
    apply(f, new com.google.common.collect.MapMaker().softValues().makeMap[A, B]())
  }

  def apply[A, B](f: A => B, cache: scala.collection.mutable.Map[A, B]): A => B = new (A => B) {
    def apply(a: A) = {
      cache.getOrElseUpdate(a, f(a))
    }
  }

  def apply[A1, A2, B](f: (A1, A2) => B): (A1, A2) => B = {
    import scala.collection.JavaConversions._
    apply(f, new com.google.common.collect.MapMaker().makeMap[(A1, A2), B]())
  }

  def softly[A1, A2, B](f: (A1, A2) => B): (A1, A2) => B = {
    import scala.collection.JavaConversions._
    apply(f, new com.google.common.collect.MapMaker().softValues().makeMap[(A1, A2), B]())
  }

  def apply[A1, A2, B](f: (A1, A2) => B, cache: scala.collection.mutable.Map[(A1, A2), B]): (A1, A2) => B = new ((A1, A2) => B) {
    def apply(a1: A1, a2: A2) = {
      cache.getOrElseUpdate((a1, a2), f(a1, a2))
    }
  }
}