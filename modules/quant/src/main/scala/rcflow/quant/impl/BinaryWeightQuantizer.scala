package rcflow.quant.impl

import rcflow.quant.api.*

object BinaryWeightQuantizer extends Quantizer[Double]:
  type B = Byte

  override val format: QFormat = QFormat.Binary

  inline def quantize(x: Double): Byte = if x >= 0.0 then 1.toByte else 0.toByte

  inline def dequantize(b: Byte): Double = if b == 0 then -1.0 else 1.0

  override val range: (Double, Double) = (-1.0, 1.0)
