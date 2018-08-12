
import matryoshka.data.Mu

package object intuition {

  type Bool = Mu[BoolF]

  type Proposition[A] = PropositionF[A, Bool]

  object Bool extends BoolFFunctions[Bool]

  object Proposition extends PropositionFFunctions[Bool]

  case class Report(report: String, evaluation: Boolean)

}
