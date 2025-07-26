package rcflow.examples

import rcflow.core._
import breeze.linalg._

object LinearRegression extends App {

  val xs = (0 to 100).map(_.toDouble)
  val ys = DenseVector(xs.map(_ * 4.5 + 2.0).toArray).toDenseMatrix.t

  val res = Reservoir(size = 64, spectralRadius = 0.2, inputScale = 0.2, seed = 2024)
  val model = Trainer.fit(res, xs, ys)

  val pred = model.predict(xs)
  println(s"NRMSE = ${Metrics.nrmse(ys, pred)}")
}
