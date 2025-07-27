package rcflow.quant.impl

import rcflow.quant.api.*
import scala.math.{pow, round}

final class FixedPointQuantizer(totalBits: Int, fracBits: Int) extends Quantizer[Double]:

  require(totalBits <= 32, "supports up to 32-bit storage")

  override type B = Int
  private val scale = 1 << fracBits
  private val minValR = -(1 << (totalBits - 1))
  private val maxValR = (1 << (totalBits - 1)) - 1

  override val format: QFormat = QFormat.Fixed(totalBits, fracBits)

  inline private def clip(raw: Long): Int =
    if raw < minValR then minValR
    else if raw > maxValR then maxValR
    else raw.toInt

  override def quantize(x: Double): Int =
    val q = round(x * scale).toLong
    clip(q)

  override def dequantize(b: Int): Double = b.toDouble / scale

  private val minReal = minValR.toDouble / scale
  private val maxReal = maxValR.toDouble / scale
  override val range: (Double, Double) = (minReal, maxReal)
