package rcflow.core

import breeze.linalg._
import rcflow.core.graph.Node

final class RLSReadout(
    inDim: Int,
    val outDim: Int,
    alpha: Double = 1e-6,
    forgetting: Double = 1.0,
    bias: Boolean = true
) extends Node {

  private val dim: Int = inDim + (if (bias) 1 else 0)
  private val W: DenseMatrix[Double] = DenseMatrix.zeros[Double](dim, outDim)
  private var P: DenseMatrix[Double] = DenseMatrix.eye[Double](dim) / alpha
  private var xHat: DenseVector[Double] = DenseVector.zeros[Double](dim)
  private var yHat: DenseVector[Double] = DenseVector.zeros[Double](outDim)

  override def forward(xv: DenseVector[Double]): DenseVector[Double] = {
    xHat = if (bias) DenseVector.vertcat(DenseVector(1.0), xv) else xv
    yHat = (W.t * xHat).toDenseVector
    yHat
  }

  def train(target: Double, learnEvery: Int = 1, stepCnt: Long): Unit = {
    if (stepCnt % learnEvery != 0L) return
    val e: Double = yHat(0) - target
    val k: DenseVector[Double] = P * xHat
    val rPr: Double = xHat dot k
    val c1: Double = 1.0 / (forgetting + rPr)
    val c2: Double = 1.0 / forgetting
    P = c2 * P - c1 * (k * k.t)
    W(::, 0) :-= e * (P * xHat)
  }
}
