import munit.FunSuite
import rcflow.core._
import breeze.linalg._

class RidgeReadoutSuite extends FunSuite {
  test("fit & predict") {
    val rng = new scala.util.Random(0)

    val T = 100; val N = 20; val M = 3

    val S = DenseMatrix.tabulate(T, N) { (_, _) => rng.nextDouble() }
    val trueW = DenseMatrix.tabulate(N, M) { (_, _) => rng.nextDouble() }

    val Y = S * trueW

    val rr = RidgeReadout().fit(S, Y)
    val pred = rr.predict(S)

    val err = Metrics.nrmse(Y, pred)
    assert(err < 1e-3, s"NRMSE too high: $err")
  }
}
