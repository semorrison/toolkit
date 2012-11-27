package net.tqft.toolkit.algebra.diophantine

import net.tqft.toolkit.algebra._
import net.tqft.toolkit.algebra.polynomials._
import scala.collection.GenSeq
import scala.collection.GenTraversableOnce

object BoundedDiophantineSolver extends net.tqft.toolkit.Logging {

  // not exactly integer polynomial programming;
  // we try to find positive integer roots of the polynomials

  // the order of variables matters; if we need to split into cases, we prefer splitting the first variables first.
  def solve[V: Ordering](polynomials: GenTraversableOnce[MultivariablePolynomial[Int, V]], variables: Seq[V], boundary: Option[Map[V, Int] => Boolean] = None, knownSolution: Option[Map[V, Int]] = None): (Iterable[Map[V, Int]], Iterable[Seq[MultivariablePolynomial[Int, V]]]) = {

    // make sure all the hash codes are computed
    //    polynomials.par.map(_.hashCode)

    type P = MultivariablePolynomial[Int, V]
    val polynomialAlgebra: MultivariablePolynomialAlgebra[Int, V] = implicitly

    case class Equations(substitutions: Map[V, P], equations: Seq[P]) {
      if ((equations.nonEmpty && equations.size % 1000 == 0) || (substitutions.nonEmpty && substitutions.size % 1000 == 0)) info(substitutions.size + " " + equations.size)
      //      equations.headOption.map(info(_))
      //      require(equations.flatMap(_.variables).toSet.intersect(substitutions.keySet).isEmpty)

      def addSubstitution(v: V, k: Int): Option[Equations] = {
        //        knownSolution map { solution =>
        //          require(solution.get(v).getOrElse(k) == k)
        //        }
        addSubstitution(v, polynomialAlgebra.constant(k))
      }
      def addSubstitution(v: V, p: P): Option[Equations] = {
        if (substitutions.keySet.contains(v)) {
          val updated = if (p.totalDegree.getOrElse(0) < substitutions(v).totalDegree.getOrElse(0)) {
            copy(substitutions = substitutions + (v -> p))
          } else {
            this
          }

          updated.addEquation(polynomialAlgebra.subtract(p, substitutions(v)))
        } else {
          val newSubstitutions = substitutions.mapValues(q => polynomialAlgebra.substitute(Map(v -> p))(q))
          val (toReprocess, toKeep) = equations.partition(_.variables.contains(v))

          Equations(newSubstitutions + (v -> p), toKeep).addEquations(toReprocess.par.map(polynomialAlgebra.substitute(Map(v -> p))))
        }
      }
      def addEquations(qs: GenTraversableOnce[P]): Option[Equations] = {
        qs /*.seq.sortBy(p => (p.totalDegree, p.terms.size))*/ .foldLeft[Option[Equations]](Some(this))({ (o, e) => o.flatMap(_.addEquation(e)) })
      }
      def addEquation(q: P): Option[Equations] = {
        val p = polynomialAlgebra.substitute(substitutions)(q).divideByCoefficientGCD

        def splitIfAllPositive: Option[Equations] = {
          if (p.terms.size > 1 && (p.terms.forall(_._2 > 0) || p.terms.forall(_._2 < 0))) {
            val xs = for (t <- p.terms; x = polynomialAlgebra.monomial(t._1)) yield x
            addEquations(xs)
          } else {
            None
          }
        }

        def splitIfPositiveOrOtherwiseAdd: Option[Equations] = {
          splitIfAllPositive.orElse({
            if (equations.contains(p)) {
              Some(this)
            } else {
              Some(copy(equations = p +: equations))
            }
          })
        }

        p.totalDegree match {
          case None => Some(this)
          case Some(0) => {
            val r = p.constantTerm
            require(r != 0)
            None
          }
          case Some(1) => {
            if (p.terms.size == 1) {
              val t = p.terms.head
              require(t._2 != 0)
              val v = t._1.keysIterator.next
              addSubstitution(v, 0)
            } else if (p.terms.size == 2) {
              if (p.constantTerm != 0) {
                // a+bV == 0
                val a = p.constantTerm
                val t = p.termsOfDegree(1).head
                val v = t._1.keysIterator.next
                val b = t._2
                if (a % b != 0 || a / b > 0) {
                  None
                } else {
                  addSubstitution(v, -a / b)
                }
              } else {
                // a_1 v_1 + a_2 v_2 == 0
                val List(t1, t2) = p.terms.toList
                val a1 = t1._2
                val a2 = t2._2
                val v1 = t1._1.keysIterator.next
                val v2 = t2._1.keysIterator.next
                if (a1 % a2 == 0) {
                  if (a1 / a2 > 0) {
                    addSubstitution(v2, 0)
                  } else {
                    addSubstitution(v2, polynomialAlgebra.monomial(Map(v1 -> 1), -a1 / a2))
                  }
                } else {
                  if (a2 % a1 == 0) {
                    if (a2 / a1 > 0) {
                      addSubstitution(v1, 0)
                    } else {
                      addSubstitution(v1, polynomialAlgebra.monomial(Map(v2 -> 1), -a2 / a1))
                    }
                  } else {
                    splitIfPositiveOrOtherwiseAdd
                  }
                }
              }
            } else {
              // nothing to do now
              splitIfPositiveOrOtherwiseAdd
            }
          }
          case Some(d) => {
            p.terms match {
              case h :: Nil => {
                // just one term, set each variable to zero
                require(h._2 != 0)
                h._1.keys.toSeq match {
                  case Seq(v) => addSubstitution(v, 0)
                  case _ => splitIfPositiveOrOtherwiseAdd
                }
              }
              case _ => {
                // nothing to do for now
                splitIfPositiveOrOtherwiseAdd
              }
            }
          }
        }
      }

      def solveALinearEquation: Option[Equations] = {
        def solve(p: P): Option[Equations] = {
          val t = p.termsOfDegree(1).find(_._2 == 1).get
          val v = t._1.keysIterator.next
          addSubstitution(v, polynomialAlgebra.subtract(polynomialAlgebra.monomial(v), p))
        }

        equations.find(p => p.totalDegree == Some(1) && p.termsOfDegree(1).exists(_._2 == 1) && p.terms.count(_._2 > 0) == 1) match {
          case Some(p) => solve(p)
          case None => {
            equations.find(p => p.totalDegree == Some(1) && p.termsOfDegree(1).exists(_._2 == -1) && p.terms.count(_._2 < 0) == 1) match {
              case Some(p) => solve(polynomialAlgebra.negate(p))
              case None => Some(this)
            }
          }
        }
      }
      def solveLinearEquations: Option[Equations] = {
        import net.tqft.toolkit.functions.FixedPoint
        FixedPoint({ o: Option[Equations] => o.flatMap(_.solveALinearEquation) })(Some(this))
      }

      def caseBashOneStep(v: V, remainingVariables: List[V]): Iterable[Equations] = {
        val limit = boundary.get

        val minimalSubstitutionBase = {
          val newSubstitutions = remainingVariables.map(w => w -> polynomialAlgebra.constant(0)).toMap
          substitutions.mapValues(p => polynomialAlgebra.substitute(newSubstitutions)(p)) ++ newSubstitutions
        }
        def minimalSubstitution(k: Int) = {
          minimalSubstitutionBase.mapValues(p => polynomialAlgebra.substituteConstants(Map(v -> k))(p).constantTerm) + (v -> k)
        }

        val cases = Iterator.from(0).takeWhile({ k => limit(minimalSubstitution(k)) }).toSeq
        info(Seq.fill(variables.indexOf(v))(" ").mkString + "case bashing " + v + " via " + cases.size + " cases, " + equations.size + " remaining equations")
        (for (k <- cases.par; r <- addSubstitution(v, k).flatMap(_.solveLinearEquations)) yield r).seq
      }
      def caseBash: Iterable[Equations] = {
        variables.filterNot(substitutions.keySet.contains).toList match {
          case v :: remainingVariables => {
            (for (c1 <- caseBashOneStep(v, remainingVariables).par; c2 <- c1.caseBash) yield c2).seq
          }
          case Nil => List(this)
        }
      }
    }

    val iterable = Equations(Map.empty, Seq.empty).addEquations(polynomials).flatMap(_.solveLinearEquations) match {
      case None => {
        //        ???
        Iterable.empty
      }
      case Some(equations) => equations.caseBash
    }

    (iterable.map(_.substitutions.mapValues(p => p.ensuring(_.totalDegree.getOrElse(0) == 0).constantTerm)), Nil)
  }
}