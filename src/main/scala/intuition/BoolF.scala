package intuition

import scalaz.Equal
import intuition.BoolF._
import matryoshka._
import scalaz.Functor

sealed trait BoolF[T]

object BoolF extends BoolFInstances {

  case class True[T]() extends BoolF[T]

  case class False[T]() extends BoolF[T]

  case class Not[T](p: T) extends BoolF[T]

  case class And[T](p: T, q: T) extends BoolF[T]

  case class Or[T](p: T, q: T) extends BoolF[T]

  case class IfThenElse[T](p: T, q: T, r: T) extends BoolF[T]

  case class Definition[T](name: String, p: T) extends BoolF[T]

  case class Contradiction(track: String) extends RuntimeException
}

abstract class BoolFFunctions[T](implicit cor: Corecursive.Aux[T, BoolF]) { functions =>

  def definition(p: T, str: String): T =
    cor.embed(Definition(str, p))

  def veritas: T =
    cor.embed(True[T]())

  def falsum: T =
    cor.embed(False[T]())

  def not(p: T): T =
    cor.embed(Not[T](p))

  def /\(p: T, q: T): T =
    cor.embed(And[T](p, q))

  def \/(p: T, q: T): T =
    cor.embed(Or[T](p, q))

  def ==>(p: T, q: T): T =
    \/(not(p), q)

  def xor(p: T, q: T): T =
    /\(\/(p, q), not(/\(p, q)))

  def cond[A](p: Boolean): T =
    if(p) veritas else falsum

  def =:=[A](x: A, y: A)(implicit eq: Equal[A]): T =
    cond(eq.equal(x, y))

  class OpsForBool(p: T) {

    def not: T = functions.not(p)

    def /\(q: T): T = functions./\(p, q)

    def \/(q: T): T = functions.\/(p, q)

    def ==>(q: T): T = functions.==>(p, q)

    def xor(q: T): T = functions.xor(p, q)
  }

  class OpsForAny[A](x: => A) {

    def =:=(y: => A)(implicit ev1: Equal[A]): T =
      functions.=:=[A](x, y)
  }
}

private[intuition] trait BoolFInstances {

  implicit val stdFunctorOfTruth: Functor[BoolF] =
    new Functor[BoolF] {
      override def map[A, B](fa: BoolF[A])(f: A => B): BoolF[B] =
        fa match {
          case True() => True()
          case False() => False()
          case Not(p) => Not(f(p))
          case And(p, q) => And(f(p), f(q))
          case Or(p, q) => Or(f(p), f(q))
          case IfThenElse(p, q, r) => IfThenElse(f(p), f(q), f(r))
          case Definition(name, p) => Definition(name, f(p))
        }
    }
}
