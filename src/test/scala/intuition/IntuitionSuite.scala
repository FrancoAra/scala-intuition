package intuition

import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.typelevel.discipline.scalatest.Discipline

trait AsyncIntuitionSuite extends AsyncFunSpec with CommonIntuitionSuite

trait IntuitionSuite extends FunSuite with Discipline with CommonIntuitionSuite

trait CommonIntuitionSuite extends Matchers
  with GeneratorDrivenPropertyChecks
