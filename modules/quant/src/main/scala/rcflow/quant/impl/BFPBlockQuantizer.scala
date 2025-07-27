package rcflow.quant.impl

import rcflow.quant.api.*
import breeze.linalg.DenseVector
import scala.reflect.ClassTag
import scala.math.*

final class BFPBlockQuantizer(
    val block: Int,
    val expBits: Int,
    val manBits: Int
):
  require(block >= 2, "block must be ≥2")
  require(expBits >= 2 && manBits >= 1)
  require(expBits + manBits <= 16)

  private val bias = (1 << (expBits - 1)) - 1
  private val manMask = (1 << manBits) - 1
  private val maxExpVal = (1 << expBits) - 2

  def quantize(vec: DenseVector[Double]): (Array[Int], Array[Int]) =
    val nBlocks = (vec.length + block - 1) / block
    val exps = Array.ofDim[Int](nBlocks)
    val mants = Array.ofDim[Int](vec.length)

    var blk = 0
    while blk < nBlocks do
      val start = blk * block
      val end = min(start + block, vec.length)

      var maxAbs = 0.0; var i = start
      while i < end do
        val a = abs(vec(i)); if a > maxAbs then maxAbs = a; i += 1
      val e =
        if maxAbs == 0.0 then 0
        else min(maxExpVal, ceil(log(maxAbs) / log(2)).toInt + bias)
      exps(blk) = e
      val scale = pow(2.0, e - bias)

      i = start
      while i < end do
        val v = vec(i)
        if v == 0.0 then mants(i) = 0
        else
          val sign = if v < 0 then 1 else 0
          val mantReal = min(1.0, abs(v) / scale)
          val mantInt = round(mantReal * manMask).toInt
          mants(i) = (sign << manBits) | mantInt
        i += 1
      blk += 1
    (exps, mants)

  def dequantize(exps: Array[Int], mants: Array[Int])(using ClassTag[Double]): DenseVector[Double] =
    val out = Array.ofDim[Double](mants.length)
    val nBlocks = exps.length

    var blk = 0
    while blk < nBlocks do
      val start = blk * block
      val end = min(start + block, mants.length)
      val scale = pow(2.0, exps(blk) - bias)

      var i = start
      while i < end do
        val word = mants(i)
        if word == 0 then out(i) = 0.0
        else
          val sign = if ((word >>> manBits) & 0x1) == 1 then -1.0 else 1.0
          val mantFrac = (word & manMask).toDouble / manMask
          out(i) = sign * mantFrac * scale
        i += 1
      blk += 1
    DenseVector(out)

  val format: QFormat = QFormat.BFP(block, expBits, manBits)
