package rcflow.core

import breeze.linalg.*
import rcflow.core.graph.Node

final class RidgeReadout(
    val inDim: Int,
    val outDim: Int,
    val ridge: Double = 1e-6
) extends Node {

  private var Wout = DenseMatrix.zeros[Double](inDim, outDim)

  private def solveSPD(A: DenseMatrix[Double], B: DenseMatrix[Double]): DenseMatrix[Double] = {
    val L = cholesky(A)
    val y = L \ B
    L.t \ y
  }

  def fit(states: DenseMatrix[Double], targets: DenseMatrix[Double]): this.type = {
    require(states.rows == targets.rows, "length mismatch")
    require(states.cols == inDim, s"inDim mismatch: expected $inDim, got ${states.cols}")
    require(targets.cols == outDim, s"outDim mismatch: expected $outDim, got ${targets.cols}")

    val sT = states.t
    val gram = sT * states
    diag(gram) :+= ridge
    val rhs = sT * targets
    this.Wout = solveSPD(gram, rhs)
    this
  }

  def forward(x: DenseVector[Double]): DenseVector[Double] = {
    (Wout.t * x).toDenseVector
  }

  def predict(states: DenseMatrix[Double]): DenseMatrix[Double] = {
    states * this.Wout
  }

  override def reset(): Unit = ()
}
