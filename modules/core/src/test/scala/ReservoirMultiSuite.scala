import munit.FunSuite
import rcflow.core._
import breeze.linalg._
import Metrics._

class ReservoirMultiSuite extends FunSuite {

  test("multi-output regression") {

    val res = Reservoir(size = 80, spectralRadius = 0.1, inputScale = 0.1, seed = 0)
    val xs = (0 until 200).map(_.toDouble)
    val ys = DenseMatrix.horzcat(
      DenseVector(xs.map(_ * 2.0).toArray).toDenseMatrix.t,
      DenseVector(xs.map(x => -3 * x + 5).toArray).toDenseMatrix.t
    )

    val model = Trainer.fit(res, xs, ys)
    val pred = model.predict(xs)

    val e1 = nrmse(ys, pred)
    assert(e1 < 0.05, s"NRMSE too high: $e1")
  }
}
