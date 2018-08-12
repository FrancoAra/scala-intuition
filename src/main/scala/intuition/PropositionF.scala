package intuition

import intuition.BoolF._
import matryoshka.implicits._
import matryoshka.{Algebra, Corecursive, Recursive}
import scalaz.{Contravariant, Order}
import scalaz.Scalaz._

import scala.annotation.tailrec

case class PropositionF[A, T](check: A => T) {

  override def toString: String = "PropositionF"
}

object PropositionF extends PropositionTInstances

abstract class PropositionFFunctions[T](implicit cor: Corecursive.Aux[T, BoolF]) extends BoolFFunctions[T]()(cor) { functions =>

  def apply[A](f: A => T): PropositionF[A, T] =
    new PropositionF[A, T](f)

  def tautology[A]: PropositionF[A, T] =
    PropositionF[A, T](_ => veritas)

  def contradiction[A]: PropositionF[A, T] =
    PropositionF[A, T](_ => falsum)

  def define[A](name: String, f: A => T): PropositionF[A, T] =
    new PropositionF[A, T](a => definition(f(a), name))

  def tag[A](name: String, p: PropositionF[A, T]): PropositionF[A, T] =
    new PropositionF[A, T](a => definition(p.check(a), name))

  def lessThan[A](other: A)(implicit ord: Order[A]): PropositionF[A, T] =
    define("LessThan", (a: A) => cond(a <= other))

  def greaterThan[A](other: A)(implicit ord: Order[A]): PropositionF[A, T] =
    define("GreaterThan", (a: A) => cond(a <= other))

  val positiveInt: PropositionF[Int, T] =
    define("PositiveInt", (a: Int) => cond(a > 0))

  def sameLength[A](xs: List[A]): PropositionF[List[A], T] =
    define("SameLength", (ys: List[A]) => =:=(xs.length, ys.length))

  def areOrdered[A](implicit ord: Order[A]): PropositionF[List[A], T] =
    define("Ordered", { xs: List[A] =>
      @tailrec def ordered(xs0: List[A]): T =
        xs0 match {
          case x0 :: x1 :: ys =>
            if (x0 <= x1) ordered(x1 :: ys)
            else falsum
          case _ :: Nil =>
            veritas
          case Nil =>
            veritas
        }
      ordered(xs)
    })

  class OpsForString(name: String) {

    def =/\=[A](p: PropositionF[A, T]): PropositionF[A, T] =
      tag(name, p)
  }

  class OpsForProposition[A](p: PropositionF[A, T]) {

    def contramap[B](f: B => A): PropositionF[B, T] =
      PropositionF(f andThen p.check)

    def in[B](f: B => A): PropositionF[B, T] =
      contramap(f)

    def not: PropositionF[A, T] =
      PropositionF(a => functions.not(p.check(a)))

    def /\(q: PropositionF[A, T]): PropositionF[A, T] =
      op(q)(functions./\)

    def \/(q: PropositionF[A, T]): PropositionF[A, T] =
      op(q)(functions.\/)

    def ==>(q: PropositionF[A, T]): PropositionF[A, T] =
      op(q)(functions.==>)

    def xor(q: PropositionF[A, T]): PropositionF[A, T] =
      op(q)(functions.xor)

    def forAllInList: PropositionF[List[A], T] =
      PropositionF[List[A], T] { xs: List[A] =>
        definition(xs.foldLeft(functions.veritas)((x, y) => functions./\(x, p.check(y))), "ForAllInList")
      }

    def existsInList: PropositionF[List[A], T] =
      PropositionF[List[A], T] { xs: List[A] =>
        definition(xs.foldLeft(functions.falsum)((x, y) => functions.\/(x, p.check(y))), "ExistsInList")
      }

    private def op(q: PropositionF[A, T])(f: (T, T) => T): PropositionF[A, T] =
      PropositionF(a => f(p.check(a), q.check(a)))

    def check[R](a: A)(algebra: Algebra[BoolF, R])(implicit rec: Recursive.Aux[T, BoolF]): R =
      p.check(a).cata(algebra)

    def apply(a: A)(implicit rec: Recursive.Aux[T, BoolF]): Boolean =
      check(a)(eval)

    def track(a: A)(implicit rec: Recursive.Aux[T, BoolF]): Report =
      report(check(a)(tracker))
  }

  private def eval: Algebra[BoolF, Evaluation] = {
    case True() => true
    case False() => false
    case Not(p) => !p
    case And(p, q) => p && q
    case Or(p, q) => p || q
    case IfThenElse(p, q, r) => if(p) q else r
    case Definition(_, p) => p
  }

  private type DefinitionName = String

  private type LatestDefinition = String

  private type Definitions = List[(DefinitionName, LatestDefinition)]

  private type Evaluation = Boolean

  private def tracker: Algebra[BoolF, (LatestDefinition, Definitions, Evaluation)] = {
    case True() =>
      ("true", Nil, true)

    case False() =>
      ("false", Nil, false)

    case Not((latest, definitions, p)) =>
      (s"!$latest", definitions, !p)

    case And((pLatest, pDefinitions, p), (qLatest, qDefinitions, q)) =>
      (s"($pLatest /\\ $qLatest)", pDefinitions ++ qDefinitions, p && q)

    case Or((pLatest, pDefinitions, p), (qLatest, qDefinitions, q)) =>
      (s"($pLatest \\/ $qLatest)", pDefinitions ++ qDefinitions, p || q)

    case IfThenElse((pLatest, pDefinitions, p), (qLatest, qDefinitions, q), (rLatest, rDefinitions, r)) =>
      (s"if ($pLatest) then ($qLatest) else ($rLatest)", pDefinitions ++ qDefinitions ++ rDefinitions, if (p) q else r)

    case Definition(name, (latest, definitions, p)) =>
      val evalSymbol =
        if(p) Console.GREEN + "☑ " + Console.RESET
        else Console.RED + "☒ " + Console.RESET
      val name0 = evalSymbol + Console.MAGENTA + name + Console.RESET
      (s"$name0", (s"$name0", latest) :: definitions, p)
  }

  private def report(result: (LatestDefinition, Definitions, Evaluation)): Report = {
    val (_, definitionsP, evaluation) = result
    val report = definitionsP.map {
      case (name1, definition) =>
        s"$name1 =/\\= $definition"
    }.mkString("\n")
    val evReport =
      if (evaluation) Console.GREEN + "Verification passed:\n" + Console.RESET
      else Console.RED + "Verification failed:\n" + Console.RESET
    Report(evReport + report, evaluation)
  }

}

private[intuition] trait PropositionTInstances {

  implicit def stdContravariant[T](implicit cor: Recursive.Aux[T, BoolF]): Contravariant[PropositionF[?, T]] =
    new Contravariant[PropositionF[?, T]] {
      override def contramap[A, B](fa: PropositionF[A, T])(f: B => A): PropositionF[B, T] =
        PropositionF(f andThen fa.check)
    }
}