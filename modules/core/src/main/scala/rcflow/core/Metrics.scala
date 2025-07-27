package rcflow.core

import breeze.linalg._
import breeze.math.Complex
import scala.util.Random

object Metrics {
  def mse(y: DenseMatrix[Double], yHat: DenseMatrix[Double]): Double = meanSq(y - yHat)

  def rmse(y: DenseMatrix[Double], yHat: DenseMatrix[Double]): Double =
    math.sqrt(mse(y, yHat))

  def nrmse(y: DenseMatrix[Double], yHat: DenseMatrix[Double]): Double = {
    val span = maxVal(y) - minVal(y)
    rmse(y, yHat) / (if (span < 1e-12) 1e-12 else span)
  }

  def r2(y: DenseMatrix[Double], yHat: DenseMatrix[Double]): Double = {
    val ssRes = sum((y - yHat) *:* (y - yHat))
    val meanY = sum(y) / y.size.toDouble
    val ssTot = sum((y - meanY) *:* (y - meanY))

    if (ssTot < 1e-12) {
      println(s"Warning: Target data has no variance (ssTot=$ssTot). R² is undefined.")
      if (ssRes < 1e-12) 1.0 else Double.NaN
    } else {
      1.0 - ssRes / ssTot
    }
  }

  def effectiveSpectralRadius(W: DenseMatrix[Double], lr: Double = 1.0): Double = {
    require(W.rows == W.cols, "W must be square")
    val A = W *:* lr + DenseMatrix.eye[Double](W.rows) * (1.0 - lr)
    val evs = eig(A).eigenvalues
    evs.data.map(math.abs).max
  }

  def memoryCapacity(
      reservoir: Reservoir,
      kMax: Int,
      seqLen: Int = 10000,
      seed: Long = 0L
  ): Double = {

    val rng = new Random(seed)
    val inputs = scala.collection.immutable.Vector.fill(seqLen)(rng.between(-0.8, 0.8))

    reservoir.reset()
    val Sfull = reservoir.run(inputs)

    val S = Sfull(kMax until seqLen, ::).toDenseMatrix
    val effLen = S.rows

    val yArr = Array.ofDim[Double](effLen * kMax)
    var col = 0
    for (k <- 1 to kMax) {
      var row = 0
      val off = kMax - k
      while (row < effLen) {
        yArr(row + col) = inputs(row + off)
        row += 1
      }
      col += effLen
    }

    val Y = new DenseMatrix(effLen, kMax, yArr)

    val readout = new RidgeReadout(inDim = S.cols, outDim = Y.cols).fit(S, Y)
    val pred = readout.predict(S)

    (0 until kMax).map { j =>
      val y = Y(::, j)
      val yp = pred(::, j)
      val yc = y - meanVec(y)
      val pc = yp - meanVec(yp)
      val r = sum(yc *:* pc) / (vnorm(yc) * vnorm(pc) + 1e-12)
      r * r
    }.sum
  }

  private def vnorm(v: DenseVector[Double]): Double = math.sqrt(sum(v *:* v))

  private def meanVec(v: DenseVector[Double]): Double = sum(v) / v.length.toDouble

  private def meanSq(m: DenseMatrix[Double]): Double = sum(m *:* m) / m.size.toDouble

  private def maxVal(m: DenseMatrix[Double]): Double = breeze.linalg.max(m)

  private def minVal(m: DenseMatrix[Double]): Double = breeze.linalg.min(m)
}
