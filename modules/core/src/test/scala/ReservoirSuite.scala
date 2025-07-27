import munit.FunSuite
import breeze.linalg._
import breeze.stats.mean
import rcflow.core._

class ReservoirSuite extends FunSuite {

  test("Trainer learns simple y = 2x") {
    val res = new Reservoir(
      size = 64,
      spectralRadius = 0.1,
      inputScale = 0.1,
      seed = 123L
    )

    val xs: Seq[Double] = (0 until 50).map(_.toDouble)
    val ysVec: DenseVector[Double] = DenseVector(xs.map(_ * 2.0).toArray)

    val model = Trainer.fit(res, xs, ysVec.toDenseMatrix.t)

    val preds: DenseVector[Double] = model.predict(xs)(::, 0)
    val diff: DenseVector[Double] = preds - ysVec

    val mseVal: Double = mean(diff *:* diff)

    assert(mseVal < 1.0, s"MSE too high: $mseVal")
  }
}
