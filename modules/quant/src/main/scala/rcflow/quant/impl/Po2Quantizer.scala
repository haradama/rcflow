package rcflow.quant.impl

import rcflow.quant.api._

final class Po2Quantizer(expBits: Int) extends Quantizer[Double] {
  require(expBits >= 1 && expBits <= 7)

  private val kMin = -(1 << (expBits - 1))
  private val kMax = 0

  type B = Byte
  override val format = QFormat.Po2(expBits)

  private def pack(sign: Int, exp: Int): Byte = {
    ((sign << expBits) | (exp & ((1 << expBits) - 1))).toByte
  }

  private def unpack(b: Byte): (Int, Int) = {
    val v = b & 0xff
    val s = v >> expBits
    val e = v & ((1 << expBits) - 1)
    (s, e)
  }

  def quantize(x: Double): Byte = {
    if (x == 0.0) {
      pack(0, (1 << expBits) - 1)
    } else {
      val sign = if (x < 0) 1 else 0
      val abs = math.abs(x)
      val k = math.max(kMin, math.min(kMax, math.round(math.log(abs) / math.log(2)).toInt))
      pack(sign, k - kMin)
    }
  }

  def dequantize(b: Byte): Double = {
    val (s, e) = unpack(b)
    if (e == ((1 << expBits) - 1)) {
      0.0
    } else {
      val k = e + kMin
      val v = math.pow(2.0, k.toDouble)
      if (s == 1) -v else v
    }
  }

  override val range = (-math.pow(2.0, kMax.toDouble), math.pow(2.0, kMax.toDouble))
}
