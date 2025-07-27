package rcflow.core

import breeze.linalg._
import rcflow.core.graph.Node

final class LMSReadout(
    inDim: Int,
    val outDim: Int,
    alpha: Double = 0.1,
    bias: Boolean = true
) extends Node {

  private val dim: Int = inDim + (if (bias) 1 else 0)
  private val W: DenseMatrix[Double] = DenseMatrix.zeros[Double](dim, outDim)
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
    val n: Double = (xHat dot xHat) + 1e-9
    W(::, 0) :-= (alpha * e / n) * xHat
  }
}
