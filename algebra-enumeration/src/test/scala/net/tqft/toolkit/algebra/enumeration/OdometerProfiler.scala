package net.tqft.toolkit.algebra.enumeration

import net.tqft.toolkit.Profiler

object OdometerProfiler extends Profiler {
  def main(args : Array[String]) : Unit = {
  	  val carLimit = { l: List[Int] => !(l.exists(_ > 9)) }
	  
	  def f = Odometer(carLimit)(List.fill(6)(0)).size  
	  
	  println(f)
	  
	  val period = 20
	  for(t <- movingTimingAverages(period)(f)) println(t)
	  
  }
}
