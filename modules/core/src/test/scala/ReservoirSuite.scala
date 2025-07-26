import munit.FunSuite
import rcflow.core._
import breeze.linalg._
import breeze.stats.mean

class ReservoirSuite extends FunSuite {

  test("Trainer learns simple y = 2x") {
    val res = Reservoir(
      size = 64,
      spectralRadius = 0.1,
      inputScale = 0.1,
      seed = 123
    )
    val xs = (0 until 50).map(_.toDouble)
    val ysVec = DenseVector(xs.map(_ * 2.0).toArray)

    val model = Trainer.fit(res, xs, ysVec.toDenseMatrix.t)

    val preds = model.predict(xs)(::, 0)
    val diff = preds - ysVec

    val mse = mean((diff *:* diff))

    assert(mse < 1.0, s"MSE too high: $mse")
  }
}
