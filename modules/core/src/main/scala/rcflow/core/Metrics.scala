package rcflow.core

import breeze.linalg.*
import breeze.math.Complex
import scala.util.Random

object Metrics {
  def mse(y: DenseMatrix[Double], yHat: DenseMatrix[Double]): Double = meanSq(y - yHat)
  def rmse(y: DenseMatrix[Double], yHat: DenseMatrix[Double]): Double = math.sqrt(mse(y, yHat))
  def nrmse(y: DenseMatrix[Double], yHat: DenseMatrix[Double]): Double =
    rmse(y, yHat) / (maxVal(y) - minVal(y))
  def r2(y: DenseMatrix[Double], yHat: DenseMatrix[Double]): Double = {
    val ssRes = sum((y - yHat) *:* (y - yHat))
    val meanY = sum(y) / y.size.toDouble
    val ssTot = sum((y - meanY) *:* (y - meanY))
    1.0 - ssRes / ssTot
  }

  def effectiveSpectralRadius(W: DenseMatrix[Double], lr: Double = 1.0): Double = {
    require(W.rows == W.cols, "W must be square")
    val A = W *:* lr + DenseMatrix.eye[Double](W.rows) * (1.0 - lr)
    val evs = eig(A).eigenvalues // DenseVector[Double]
    evs.data.map(math.abs).max
  }

  def memoryCapacity(
      reservoir: Reservoir,
      kMax: Int,
      seqLen: Int = 10000,
      seed: Long = 0L
  ): Double = {

    val rng = new Random(seed)
    val inputs = scala.Vector.fill(seqLen)(rng.between(-0.8, 0.8))

    val Sfull = reservoir.run(inputs)

    val S = Sfull(kMax until seqLen, ::).toDenseMatrix
    val effLen = S.rows

    val Y = DenseMatrix.zeros[Double](effLen, kMax)
    for k <- 1 to kMax do
      Y(::, k - 1) := DenseVector(
        inputs.slice(kMax - k, seqLen - k).toArray
      )

    val readout = RidgeReadout().fit(S, Y)
    val pred = readout.predict(S)

    (0 until kMax).map { j =>
      val y = Y(::, j)
      val yp = pred(::, j)
      val yc = y - meanVec(y)
      val pc = yp - meanVec(yp)
      val r = (yc dot pc) / (norm(yc) * norm(pc) + 1e-12)
      r * r
    }.sum
  }

  private def meanVec(v: DenseVector[Double]) = sum(v) / v.length.toDouble
  private def meanSq(m: DenseMatrix[Double]) = sum(m *:* m) / m.size.toDouble
  private def maxVal(m: DenseMatrix[Double]) = breeze.linalg.max(m)
  private def minVal(m: DenseMatrix[Double]) = breeze.linalg.min(m)
}
