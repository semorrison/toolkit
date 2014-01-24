package net.tqft.toolkit.collections

import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global

object MapCaching {
  implicit def map2CachableMap[A, B](map: scala.collection.mutable.Map[A, B]) = new CachableMap(map)

  class CachableMap[A, B](other: scala.collection.mutable.Map[A, B]) {
    def caching(cache: scala.collection.mutable.Map[A, B] = scala.collection.mutable.Map[A, B]()) = new scala.collection.mutable.Map[A, B] {
      override def -=(key: A): this.type = {
        future {
          other -= key
        }
        cache -= key
        this
      }
      override def +=(kv: (A, B)): this.type = {
        future {
          other += kv
        }
        cache += kv
        this
      }
      override def iterator = other.iterator
      override def get(key: A) = cache.get(key) match {
        case Some(v) => Some(v)
        case None => {
          other.get(key) match {
            case Some(v) => {
              future {
                cache += ((key, v))
              }
              Some(v)
            }
            case None => None
          }
        }
      }
      override def size = other.size
      override def foreach[U](f: ((A, B)) => U) { other.foreach[U](f) }
      override def hashCode = other.hashCode
      override def equals(that: Any) = other.equals(that)
      override def contains(key: A) = cache.contains(key) || other.contains(key)
    }
  }
}