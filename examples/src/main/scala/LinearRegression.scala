package rcflow.examples

import breeze.linalg._
import rcflow.core._

object LinearRegression {

  def main(args: Array[String]): Unit = {
    val xs: Seq[Double] = (0 to 100).map(_.toDouble)
    val ys: DenseMatrix[Double] =
      DenseVector(xs.map(x => 4.5 * x + 2.0).toArray).toDenseMatrix.t

    val res = new Reservoir(
      size = 64,
      spectralRadius = 0.2,
      inputScale = 0.2,
      seed = 2024L
    )
    val model = Trainer.fit(res, xs, ys)

    val pred: DenseMatrix[Double] = model.predict(xs)
    println(s"NRMSE = ${Metrics.nrmse(ys, pred)}")
  }
}
