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

  private val dim = inDim + (if bias then 1 else 0)
  private val W = DenseMatrix.zeros[Double](dim, outDim)
  private var P = DenseMatrix.eye[Double](dim) / alpha
  private var xHat = DenseVector.zeros[Double](dim)
  private var yHat = DenseVector.zeros[Double](outDim)

  override def forward(xv: DenseVector[Double]): DenseVector[Double] = {
    xHat = if bias then DenseVector.vertcat(DenseVector(1.0), xv) else xv
    yHat = (W.t * xHat).toDenseVector
    yHat
  }

  def train(target: Double, learnEvery: Int = 1, stepCnt: Long): Unit = {
    if stepCnt % learnEvery != 0 then return
    val e = yHat(0) - target
    val k = P * xHat
    val rPr = (xHat dot k)
    val c1 = 1.0 / (forgetting + rPr)
    val c2 = 1.0 / forgetting
    P = c2 * P - c1 * (k * k.t)
    W(::, 0) :-= e * (P * xHat)
  }
}
