package net.tqft.toolkit.algebra

trait IntegerModel[I] extends OrderedEuclideanDomain[I] {
  def toBigInt(i: I): BigInt
  def fromBigInt(i: BigInt): I
  def from[II:IntegerModel](i: II): I = {
    fromBigInt(implicitly[IntegerModel[II]].toBigInt(i))
  }  
}

trait ImplicitIntegerModels extends ImplicitEuclideanDomains {
  override implicit val Integers: IntegerModel[Int] = Gadgets.Integers
  override implicit val BigInts: IntegerModel[BigInt] = Gadgets.BigIntegers
}

object IntegerModel extends ImplicitIntegerModels
