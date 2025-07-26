import breeze.linalg._
import rcflow.core.graph.Node

final class LMSReadout(
    inDim: Int,
    val outDim: Int,
    alpha: Double = 0.1,
    bias: Boolean = true
) extends Node {

  private val dim = inDim + (if bias then 1 else 0)
  private val W = DenseMatrix.zeros[Double](dim, outDim)
  private var xHat = DenseVector.zeros[Double](dim)
  private var yHat = DenseVector.zeros[Double](outDim)
  private var stepId = 0L

  override def forward(xv: DenseVector[Double]): DenseVector[Double] = {
    xHat = if bias then DenseVector.vertcat(DenseVector(1.0), xv) else xv
    yHat = (W.t * xHat).toDenseVector
    yHat
  }

  def train(target: Double): Unit = {
    val e = yHat(0) - target

    val n = (xHat dot xHat) + 1e-9

    W(::, 0) :-= (alpha * e / n) * xHat
  }
}
