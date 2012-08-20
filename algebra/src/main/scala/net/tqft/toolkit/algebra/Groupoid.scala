package net.tqft.toolkit.algebra

trait Groupoid[O, M] extends Category[O, M] {
	def inverse(m: M): M
}