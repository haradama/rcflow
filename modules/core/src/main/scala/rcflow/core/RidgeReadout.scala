package rcflow.core

import breeze.linalg._

final case class RidgeReadout(
    ridge: Double = 1e-6,
    Wout: DenseMatrix[Double] = DenseMatrix.zeros[Double](0, 0)
) {

  def fit(states: DenseMatrix[Double], targets: DenseMatrix[Double]): RidgeReadout = {

    require(states.rows == targets.rows, s"length mismatch: ${states.rows} vs ${targets.rows}")

    val sT = states.t
    val idn = DenseMatrix.eye[Double](states.cols)
    val w = inv(sT * states + idn * ridge) * sT * targets // N×M
    copy(Wout = w)
  }

  def predict(states: DenseMatrix[Double]): DenseMatrix[Double] =
    states * Wout
}
