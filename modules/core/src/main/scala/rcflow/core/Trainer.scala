package rcflow.core

import breeze.linalg._

object Trainer {

  def fit(
      reservoir: Reservoir,
      inputs: Iterable[Double],
      targets: DenseMatrix[Double],
      ridge: Double = 1e-6
  ): TrainedModel = {

    val states = reservoir.run(inputs)
    val readout = RidgeReadout(ridge).fit(states, targets)
    TrainedModel(reservoir, readout)
  }
}

final case class TrainedModel(
    reservoir: Reservoir,
    readout: RidgeReadout
) {
  def predict(inputs: Iterable[Double]): DenseMatrix[Double] = {
    val s = reservoir.run(inputs)
    readout.predict(s)
  }
}
