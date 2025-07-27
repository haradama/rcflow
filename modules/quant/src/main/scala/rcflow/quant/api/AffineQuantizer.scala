package rcflow.quant.impl

import rcflow.quant.api.*
import RoundClip.*
import RoundClip.RoundingMode

final class AffineQuantizer(
    bits: Int,
    val scale: Double,
    val zeroPoint: Int,
    rounding: RoundingMode = RoundingMode.Nearest
) extends Quantizer[Double]:

  require(bits >= 2 && bits <= 16, "bits 2-16 supported")
  require(scale > 0, "scale must be positive")
  private val qMin = 0
  private val qMax = (1 << bits) - 1

  override type B = Short
  override val format: QFormat =
    QFormat.Affine(bits = bits, scale = scale, zeroPoint = zeroPoint)

  override def quantize(x: Double): Short =
    val q = round(x / scale + zeroPoint, rounding)
    clip(q, qMin, qMax).toShort

  override def dequantize(b: Short): Double =
    scale * (b.toInt - zeroPoint)

  override val range: (Double, Double) =
    (scale * (qMin - zeroPoint), scale * (qMax - zeroPoint))

object AffineQuantizer {

  def fromMaxAbs(bits: Int, maxAbs: Double): AffineQuantizer =
    require(maxAbs > 0, "maxAbs must be positive")
    val scale = maxAbs / ((1 << (bits - 1)) - 1)
    new AffineQuantizer(bits, scale, zeroPoint = 1 << (bits - 1))
}
