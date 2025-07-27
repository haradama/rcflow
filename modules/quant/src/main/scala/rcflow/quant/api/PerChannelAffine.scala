package rcflow.quant.api

import breeze.linalg.DenseVector
import scala.reflect.ClassTag
import rcflow.quant.impl.AffineQuantizer
import rcflow.quant.api.RoundClip.RoundingMode

object PerChannelAffine {

  def quantizeVec[QB](
      vec: DenseVector[Double],
      scales: Array[Double],
      zeros: Array[Int],
      bits: Int,
      rounding: RoundingMode = RoundingMode.Nearest
  )(implicit ct: ClassTag[QB]): Array[QB] = {
    require(vec.length == scales.length && vec.length == zeros.length)
    val out = new Array[QB](vec.length)
    var i = 0
    while (i < vec.length) {
      val q = new AffineQuantizer(bits, scales(i), zeros(i), rounding)
      out(i) = q.quantize(vec(i)).asInstanceOf[QB]
      i += 1
    }
    out
  }

  def dequantizeVec(
      data: Array[Short],
      scales: Array[Double],
      zeros: Array[Int],
      bits: Int
  ): DenseVector[Double] = {
    require(data.length == scales.length && data.length == zeros.length)
    val out = Array.ofDim[Double](data.length)
    var i = 0
    while (i < data.length) {
      val q = new AffineQuantizer(bits, scales(i), zeros(i))
      out(i) = q.dequantize(data(i))
      i += 1
    }
    DenseVector(out)
  }
}
