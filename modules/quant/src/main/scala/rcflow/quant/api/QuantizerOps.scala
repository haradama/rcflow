package rcflow.quant.api

import breeze.linalg.{DenseMatrix, DenseVector}

import rcflow.core.{Reservoir, RidgeReadout}

import scala.reflect.ClassTag

import rcflow.quant.impl.BFPBlockQuantizer

import rcflow.core.Metrics.effectiveSpectralRadius

object QuantizerOps {

  extension (q: Quantizer[Double])

    def quantizeVec(vec: DenseVector[Double])(using ClassTag[q.B]): Array[q.B] =
      val out = Array.ofDim[q.B](vec.length)
      var i = 0
      while i < vec.length do
        out(i) = q.quantize(vec(i))
        i += 1
      out

    def dequantizeVec(data: Array[q.B]): DenseVector[Double] =
      val arr = Array.ofDim[Double](data.length)
      var i = 0
      while i < data.length do
        arr(i) = q.dequantize(data(i))
        i += 1
      DenseVector(arr)

    def quantizeMat(mat: DenseMatrix[Double])(using ClassTag[q.B]): Array[q.B] =
      val rows = mat.rows; val cols = mat.cols
      val out = Array.ofDim[q.B](rows * cols)
      var idx = 0
      var c = 0
      while c < cols do
        var r = 0
        while r < rows do
          out(idx) = q.quantize(mat(r, c))
          r += 1; idx += 1
        c += 1
      out

    def dequantizeMat(data: Array[q.B], rows: Int, cols: Int): DenseMatrix[Double] =
      require(data.length == rows * cols, s"length ${data.length} ≠ rows*cols ${rows * cols}")
      val mat = DenseMatrix.zeros[Double](rows, cols)
      var idx = 0; var c = 0
      while c < cols do
        var r = 0
        while r < rows do
          mat(r, c) = q.dequantize(data(idx))
          r += 1; idx += 1
        c += 1
      mat

  def normalizeSpectralRadius(
      W: breeze.linalg.DenseMatrix[Double],
      target: Double = 0.95
  ): breeze.linalg.DenseMatrix[Double] =
    val rho = effectiveSpectralRadius(W)
    if rho < 1e-12 then W else W * (target / rho)
}

final case class QReservoir[W, I](
    size: Int,
    wFormat: QFormat,
    wInFormat: QFormat,
    wData: Array[W], // column‑major size×size
    wInData: Array[I] // length = size
)(using ClassTag[W], ClassTag[I])

final case class QReadout[B](
    inDim: Int,
    outDim: Int,
    format: QFormat,
    wData: Array[B] // column‑major inDim×outDim
)(using ClassTag[B])

object QFlow:

  import QuantizerOps.*
  def quantize[BW, BI](
      res: rcflow.core.Reservoir,
      qW: Quantizer[Double] { type B = BW },
      qIn: Quantizer[Double] { type B = BI }
  )(using ClassTag[BW], ClassTag[BI]): QReservoir[BW, BI] =
    val wField = res.getClass.getDeclaredField("w")
    wField.setAccessible(true)
    val wMat = wField.get(res).asInstanceOf[DenseMatrix[Double]]

    val wInField = res.getClass.getDeclaredField("wIn")
    wInField.setAccessible(true)
    val wInVec = wInField.get(res).asInstanceOf[DenseVector[Double]]

    val wArr = qW.quantizeMat(wMat)
    val wInArr = qIn.quantizeVec(wInVec)

    QReservoir(
      size = res.size,
      wFormat = qW.format,
      wInFormat = qIn.format,
      wData = wArr,
      wInData = wInArr
    )

  def quantize[BB](
      rr: rcflow.core.RidgeReadout,
      q: Quantizer[Double] { type B = BB }
  )(using ClassTag[BB]): QReadout[BB] =
    val wField = rr.getClass.getDeclaredField("Wout")
    wField.setAccessible(true)
    val wMat = wField.get(rr).asInstanceOf[DenseMatrix[Double]]

    QReadout(
      inDim = rr.inDim,
      outDim = rr.outDim,
      format = q.format,
      wData = q.quantizeMat(wMat)
    )

  extension (bfp: BFPBlockQuantizer)

    def quantize(vec: DenseVector[Double]): (Array[Int], Array[Int]) =
      bfp.quantize(vec)

    def dequantize(exps: Array[Int], mants: Array[Int]): DenseVector[Double] =
      bfp.dequantize(exps, mants)
