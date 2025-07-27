package rcflow.quant.impl

import rcflow.quant.api._
import rcflow.quant.api.RoundClip._
import rcflow.quant.api.RoundClip.RoundingMode

import scala.math.pow

final class FixedPointQuantizer(
    totalBits: Int,
    fracBits: Int,
    rounding: RoundingMode = RoundingMode.Nearest
) extends Quantizer[Double] {

  require(totalBits >= 2 && totalBits <= 31, "totalBits must be 2‑31 (fits into signed Int)")
  require(fracBits >= 0 && fracBits < totalBits, "fracBits must be < totalBits")

  override type B = Int

  private val scale: Long = 1L << fracBits
  private val minRaw: Long = -(1L << (totalBits - 1))
  private val maxRaw: Long = (1L << (totalBits - 1)) - 1

  override val format: QFormat = QFormat.Fixed(totalBits, fracBits)

  override def quantize(x: Double): Int = {
    val raw = round(x * scale, rounding)
    val clipRaw = clip(raw, minRaw, maxRaw)
    clipRaw.toInt
  }

  override def dequantize(b: Int): Double = {
    b.toDouble / scale
  }

  private val minReal = minRaw.toDouble / scale
  private val maxReal = maxRaw.toDouble / scale

  override val range: (Double, Double) = (minReal, maxReal)
}
