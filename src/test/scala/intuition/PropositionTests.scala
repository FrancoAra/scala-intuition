package intuition

import intuition.Proposition._
import intuition.implicits._
import org.scalatest.Matchers
import scalaz.Scalaz._

class PropositionTests extends IntuitionSuite with Matchers {

  test("Simple") {
    val IsSortedAndPositive = areOrdered[Int] /\ positiveInt.existsInList
    val xs = List(1,3,0).sorted
    val report = IsSortedAndPositive.track(xs)
    println(report.report)
    assert(report.evaluation)
  }
}
