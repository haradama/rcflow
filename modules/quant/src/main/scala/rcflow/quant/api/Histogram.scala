package rcflow.quant.api

import breeze.linalg.DenseVector
import scala.collection.mutable.ArrayBuffer


final class HistogramCalibrator(
    val numBins: Int = 2048,
    val symmetric: Boolean = true
) extends Calibrator:

  require(
    numBins >= 128 && (numBins & (numBins - 1)) == 0,
    "numBins must be power of 2 and ≥ 128 for stable KL calibration"
  )

  private val samples = ArrayBuffer.empty[Double]

  override def observe(x: Double): Unit = samples += x

  override def result(bits: Int): (Double, Double) =
    require(samples.nonEmpty, "No data observed for calibration")

    val levels = (1 << (bits - 1)) - 1
    val absMax = samples.iterator.map(math.abs).max

    val binWidth = (2 * absMax) / numBins
    val hist = new Array[Double](numBins)
    samples.foreach { v =>
      val idx = (((v + absMax) / binWidth).toInt).max(0).min(numBins - 1)
      hist(idx) += 1.0
    }

    val cdf = new Array[Double](numBins)
    var s = 0.0; var i = 0
    while i < numBins do
      s += hist(i); cdf(i) = s; i += 1

    val totalCnt = s

    var bestKl = Double.PositiveInfinity
    var bestBin = numBins - 1

    var n = 128
    while n < numBins do
      val refCnt = cdf(n)
      val refHist = hist.slice(0, n + 1)

      val qHist = new Array[Double](levels)
      var srcIdx = 0; var dstIdx = 0; var acc = 0.0; var step = (n + 1).toDouble / levels
      while dstIdx < levels do
        val end = (dstIdx + 1) * step
        while srcIdx < end && srcIdx <= n do
          acc += refHist(srcIdx.toInt); srcIdx += 1
        qHist(dstIdx) = acc; acc = 0.0; dstIdx += 1

      val refHistSum = refCnt.max(1e-8)
      val deqHist = new Array[Double](n + 1)
      dstIdx = 0; srcIdx = 0; step = (n + 1).toDouble / levels
      while dstIdx < levels do
        val binVal = qHist(dstIdx) / step
        val end = ((dstIdx + 1) * step).toInt
        while srcIdx < end && srcIdx <= n do
          deqHist(srcIdx) = binVal; srcIdx += 1
        dstIdx += 1

      var kl = 0.0; var j = 0
      while j <= n do
        val p = refHist(j)
        val q = deqHist(j)
        if p > 0.0 && q > 0.0 then kl += p * math.log(p / q)
        j += 1
      kl /= refHistSum

      if kl < bestKl then
        bestKl = kl; bestBin = n

      n += 1
    end while

    val thresh = (bestBin + 1) * binWidth / 2.0 // half‑range
    val minVal = if symmetric then -thresh else -absMax
    val maxVal = if symmetric then thresh else absMax
    (minVal, maxVal)

final class PerChannelHistogramCalibrator(
    channels: Int,
    numBins: Int = 2048,
    symmetric: Boolean = true
):

  private val cals = Array.fill(channels)(HistogramCalibrator(numBins, symmetric))

  def observe(vec: DenseVector[Double]): Unit =
    require(vec.length == channels)
    var i = 0
    while i < channels do
      cals(i).observe(vec(i)); i += 1

  def result(bits: Int): (DenseVector[Double], DenseVector[Double]) =
    val mins = Array.ofDim[Double](channels)
    val maxs = Array.ofDim[Double](channels)
    var i = 0
    while i < channels do
      val (mn, mx) = cals(i).result(bits)
      mins(i) = mn; maxs(i) = mx; i += 1
    (DenseVector(mins), DenseVector(maxs))
