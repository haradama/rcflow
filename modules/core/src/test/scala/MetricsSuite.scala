import munit.FunSuite
import rcflow.core.Metrics._
import breeze.linalg._

class MetricsSuite extends FunSuite {
  val y = DenseMatrix(1.0, 2.0, 3.0).toDenseMatrix.t
  val yH = DenseMatrix(1.0, 2.5, 3.0).toDenseMatrix.t

  test("mse/rmse") {
    assertEqualsDouble(mse(y, yH), 0.08333, 1e-4)
    assertEqualsDouble(rmse(y, yH), 0.288675, 1e-5)
  }
  test("r2") {
    assertEqualsDouble(r2(y, y), 1.0, 1e-12)

    val r = r2(y, yH)
    assert(r > 0.8, s"r2 too low: $r")
  }
}
