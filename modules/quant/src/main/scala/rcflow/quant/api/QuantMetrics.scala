package rcflow.quant.api

import breeze.linalg.{DenseVector, DenseMatrix, sum}

object QuantMetrics {

  private def mean(a: DenseVector[Double]): Double = {
    sum(a) / a.length
  }

  private def mseVec(orig: DenseVector[Double], recon: DenseVector[Double]): Double = {
    val diff = orig - recon
    sum(diff *:* diff) / diff.length
  }

  def mse(orig: DenseVector[Double], recon: DenseVector[Double]): Double = {
    require(orig.length == recon.length)
    mseVec(orig, recon)
  }

  def rmse(orig: DenseVector[Double], recon: DenseVector[Double]): Double = {
    math.sqrt(mse(orig, recon))
  }

  def maxAbsErr(orig: DenseVector[Double], recon: DenseVector[Double]): Double = {
    require(orig.length == recon.length)
    val diff = (orig - recon).data.map(math.abs)
    diff.max
  }

  def snr(orig: DenseVector[Double], recon: DenseVector[Double]): Double = {
    val errPow = mseVec(orig, recon)
    val sigPow = mean(orig *:* orig)
    10.0 * math.log10(sigPow / (errPow + 1e-20))
  }

  def psnr(orig: DenseVector[Double], recon: DenseVector[Double]): Double = {
    val peak = orig.data.map(math.abs).max
    val err = mseVec(orig, recon)
    20.0 * math.log10(peak / math.sqrt(err + 1e-20))
  }

  private def mseMat(orig: DenseMatrix[Double], recon: DenseMatrix[Double]): Double = {
    val diff = orig - recon
    sum(diff *:* diff) / diff.size
  }

  def mse(orig: DenseMatrix[Double], recon: DenseMatrix[Double]): Double = {
    require(orig.rows == recon.rows && orig.cols == recon.cols)
    mseMat(orig, recon)
  }

  def rmse(orig: DenseMatrix[Double], recon: DenseMatrix[Double]): Double = {
    math.sqrt(mse(orig, recon))
  }

  def maxAbsErr(orig: DenseMatrix[Double], recon: DenseMatrix[Double]): Double = {
    require(orig.rows == recon.rows && orig.cols == recon.cols)
    val diff = (orig - recon).data.map(math.abs)
    diff.max
  }

  def snr(orig: DenseMatrix[Double], recon: DenseMatrix[Double]): Double = {
    val errPow = mseMat(orig, recon)
    val sigPow = sum(orig *:* orig) / orig.size
    10.0 * math.log10(sigPow / (errPow + 1e-20))
  }

  def psnr(orig: DenseMatrix[Double], recon: DenseMatrix[Double]): Double = {
    val peak = orig.data.map(math.abs).max
    val err = mseMat(orig, recon)
    20.0 * math.log10(peak / math.sqrt(err + 1e-20))
  }
}
