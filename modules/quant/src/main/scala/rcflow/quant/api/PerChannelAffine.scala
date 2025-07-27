package rcflow.quant.api

import breeze.linalg.DenseVector
import scala.reflect.ClassTag
import rcflow.quant.impl.AffineQuantizer
import RoundClip.RoundingMode

object PerChannelAffine:

  def quantizeVec[QB](
      vec: DenseVector[Double],
      scales: Array[Double],
      zeros: Array[Int],
      bits: Int,
      rounding: RoundingMode = RoundingMode.Nearest
  )(using ClassTag[QB]): Array[QB] =
    require(vec.length == scales.length && vec.length == zeros.length)
    val out = new Array[QB](vec.length)
    var i = 0
    while i < vec.length do
      val q = new AffineQuantizer(bits, scales(i), zeros(i), rounding)
      out(i) = q.quantize(vec(i)).asInstanceOf[QB]
      i += 1
    out
