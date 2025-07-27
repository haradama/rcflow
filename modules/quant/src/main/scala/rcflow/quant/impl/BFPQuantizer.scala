package rcflow.quant.impl

import rcflow.quant.api._

final class BFPQuantizer(expBits: Int, manBits: Int) extends Quantizer[Double] {
  require(expBits >= 2 && manBits >= 1 && expBits + manBits <= 16)

  override type B = Int
  override val format = QFormat.BFP(1, expBits, manBits)

  private val bias = (1 << (expBits - 1)) - 1
  private val manMask = (1 << manBits) - 1

  def quantize(x: Double): Int = {
    if (x == 0.0) {
      0
    } else {
      val sign = if (x < 0) 1 else 0
      val abs = math.abs(x)

      val eD = math.floor(math.log(abs) / math.log(2)).toInt
      val bias = (1 << (expBits - 1)) - 1
      val maxE = (1 << expBits) - 1
      var eU = math.max(eD + bias, 0)
      eU = math.min(eU, maxE)

      val scale = math.pow(2, eD)
      val rawMant = ((abs / scale - 1.0) * (1 << manBits)).round.toInt
      val fullUnit = 1 << manBits
      var mant = rawMant

      if (mant == fullUnit) {
        mant = 0
        eU += 1
        if (eU > maxE) {
          eU = maxE
          mant = fullUnit - 1
        }
      }

      val manMask = fullUnit - 1
      mant &= manMask

      (sign << (expBits + manBits)) | (eU << manBits) | mant
    }
  }

  def dequantize(b: Int): Double = {
    if (b == 0) {
      0.0
    } else {
      val signBit = (b >>> (expBits + manBits)) & 0x1
      val sign = if (signBit == 1) -1.0 else 1.0
      val exp = (b >>> manBits) & ((1 << expBits) - 1)
      val mant = b & manMask
      val frac = 1.0 + mant.toDouble / (1 << manBits)
      sign * frac * math.pow(2.0, exp - bias)
    }
  }

  override val range = (Double.NegativeInfinity, Double.PositiveInfinity)
}
