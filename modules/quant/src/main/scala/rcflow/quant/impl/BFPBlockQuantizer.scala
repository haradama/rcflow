package rcflow.quant.impl

import rcflow.quant.api._
import breeze.linalg.DenseVector
import scala.reflect.ClassTag
import scala.math._

final class BFPBlockQuantizer(
    val block: Int,
    val expBits: Int,
    val manBits: Int
) {
  require(block >= 2, "block must be ≥2")
  require(expBits >= 2 && manBits >= 1)
  require(expBits + manBits <= 16)

  private val bias = (1 << (expBits - 1)) - 1
  private val manMask = (1 << manBits) - 1
  private val maxExpVal = (1 << expBits) - 2

  def quantize(vec: DenseVector[Double]): (Array[Int], Array[Int]) = {
    val nBlocks = (vec.length + block - 1) / block
    val exps = Array.ofDim[Int](nBlocks)
    val mants = Array.ofDim[Int](vec.length)

    var blk = 0
    while (blk < nBlocks) {
      val start = blk * block
      val end = math.min(start + block, vec.length)

      var maxAbs = 0.0
      var i = start
      while (i < end) {
        val a = math.abs(vec(i))
        if (a > maxAbs) maxAbs = a
        i += 1
      }

      val e =
        if (maxAbs == 0.0) 0
        else math.min(maxExpVal, math.ceil(math.log(maxAbs) / math.log(2)).toInt + bias)
      exps(blk) = e
      val scale = math.pow(2.0, e - bias)

      i = start
      while (i < end) {
        val v = vec(i)
        if (v == 0.0) {
          mants(i) = 0
        } else {
          val sign = if (v < 0) 1 else 0
          val mantReal = math.min(1.0, math.abs(v) / scale)
          val mantInt = math.round(mantReal * manMask).toInt
          mants(i) = (sign << manBits) | mantInt
        }
        i += 1
      }

      blk += 1
    }

    (exps, mants)
  }

  def dequantize(exps: Array[Int], mants: Array[Int])(implicit
      ct: ClassTag[Double]
  ): DenseVector[Double] = {
    val out = Array.ofDim[Double](mants.length)
    val nBlocks = exps.length

    var blk = 0
    while (blk < nBlocks) {
      val start = blk * block
      val end = math.min(start + block, mants.length)
      val scale = math.pow(2.0, exps(blk) - bias)

      var i = start
      while (i < end) {
        val word = mants(i)
        if (word == 0) {
          out(i) = 0.0
        } else {
          val sign = if (((word >>> manBits) & 0x1) == 1) -1.0 else 1.0
          val mantFrac = (word & manMask).toDouble / manMask
          out(i) = sign * mantFrac * scale
        }
        i += 1
      }

      blk += 1
    }

    DenseVector(out)
  }

  val format: QFormat = QFormat.BFP(block, expBits, manBits)
}
