package intuition

package object implicits {

  implicit def toBoolOps(a: Bool): Bool.OpsForBool =
    new Bool.OpsForBool(a)

  implicit def toBoolOpsForAny[A](x: A): Bool.OpsForAny[A] =
    new Bool.OpsForAny[A](x)

  implicit def toPropositionOps[A](p: Proposition[A]): Proposition.OpsForProposition[A] =
    new Proposition.OpsForProposition[A](p)

  implicit def toPropositionOpsForString(name: String): Proposition.OpsForString =
    new Proposition.OpsForString(name)
}
