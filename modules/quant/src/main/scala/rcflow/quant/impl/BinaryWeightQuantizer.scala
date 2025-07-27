package rcflow.quant.impl

import rcflow.quant.api._

object BinaryWeightQuantizer extends Quantizer[Double] {
  type B = Byte

  override val format: QFormat = QFormat.Binary

  def quantize(x: Double): Byte = {
    if (x >= 0.0) 1.toByte else 0.toByte
  }

  def dequantize(b: Byte): Double = {
    if (b == 0) -1.0 else 1.0
  }

  override val range: (Double, Double) = (-1.0, 1.0)
}
