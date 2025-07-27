package rcflow.quant.api

import breeze.linalg.{DenseMatrix, DenseVector}
import rcflow.core.{Reservoir, RidgeReadout}
import rcflow.quant.impl.BFPBlockQuantizer
import rcflow.core.Metrics.effectiveSpectralRadius

import scala.reflect.ClassTag

object QuantizerOps {

  /** Quantizer[Double] の型メンバ B を QB として取り出し、サポートする拡張メソッドを定義 */
  implicit class QuantizerVecOps[QB](val q: Quantizer[Double] { type B = QB })(implicit
      ct: ClassTag[QB]
  ) {

    def quantizeVec(vec: DenseVector[Double]): Array[QB] = {
      val out = Array.ofDim[QB](vec.length)
      var i = 0
      while (i < vec.length) {
        out(i) = q.quantize(vec(i))
        i += 1
      }
      out
    }

    def dequantizeVec(data: Array[QB]): DenseVector[Double] = {
      val arr = Array.ofDim[Double](data.length)
      var i = 0
      while (i < data.length) {
        arr(i) = q.dequantize(data(i))
        i += 1
      }
      DenseVector(arr)
    }

    def quantizeMat(mat: DenseMatrix[Double]): Array[QB] = {
      val rows = mat.rows
      val cols = mat.cols
      val out = Array.ofDim[QB](rows * cols)
      var idx = 0
      var c = 0
      while (c < cols) {
        var r = 0
        while (r < rows) {
          out(idx) = q.quantize(mat(r, c))
          r += 1
          idx += 1
        }
        c += 1
      }
      out
    }

    def dequantizeMat(data: Array[QB], rows: Int, cols: Int): DenseMatrix[Double] = {
      require(data.length == rows * cols, s"length ${data.length} ≠ rows*cols ${rows * cols}")
      val mat = DenseMatrix.zeros[Double](rows, cols)
      var idx = 0
      var c = 0
      while (c < cols) {
        var r = 0
        while (r < rows) {
          mat(r, c) = q.dequantize(data(idx))
          r += 1
          idx += 1
        }
        c += 1
      }
      mat
    }
  }

  /** Reservoir 重みの正規化 */
  def normalizeSpectralRadius(
      W: DenseMatrix[Double],
      target: Double = 0.95
  ): DenseMatrix[Double] = {
    val rho = effectiveSpectralRadius(W)
    if (rho < 1e-12) W else W * (target / rho)
  }
}

final case class QReservoir[W, I](
    size: Int,
    wFormat: QFormat,
    wInFormat: QFormat,
    wData: Array[W], // column‑major size×size
    wInData: Array[I] // length = size
)(implicit val ctW: ClassTag[W], val ctI: ClassTag[I])

final case class QReadout[B](
    inDim: Int,
    outDim: Int,
    format: QFormat,
    wData: Array[B] // column‑major inDim×outDim
)(implicit val ctB: ClassTag[B])

object QFlow {

  import QuantizerOps._

  def quantize[BW, BI](
      res: Reservoir,
      qW: Quantizer[Double] { type B = BW },
      qIn: Quantizer[Double] { type B = BI }
  )(implicit ctW: ClassTag[BW], ctI: ClassTag[BI]): QReservoir[BW, BI] = {

    val wField = res.getClass.getDeclaredField("w")
    wField.setAccessible(true)
    val wMat = wField.get(res).asInstanceOf[DenseMatrix[Double]]

    val wInField = res.getClass.getDeclaredField("wIn")
    wInField.setAccessible(true)
    val wInVec = wInField.get(res).asInstanceOf[DenseVector[Double]]

    val wArr = new QuantizerVecOps[BW](qW).quantizeMat(wMat)
    val wInArr = new QuantizerVecOps[BI](qIn).quantizeVec(wInVec)

    QReservoir(
      size = res.size,
      wFormat = qW.format,
      wInFormat = qIn.format,
      wData = wArr,
      wInData = wInArr
    )
  }

  def quantize[BB](
      rr: RidgeReadout,
      q: Quantizer[Double] { type B = BB }
  )(implicit ctB: ClassTag[BB]): QReadout[BB] = {

    val wField = rr.getClass.getDeclaredField("Wout")
    wField.setAccessible(true)
    val wMat = wField.get(rr).asInstanceOf[DenseMatrix[Double]]

    val wArr = new QuantizerVecOps[BB](q).quantizeMat(wMat)

    QReadout(
      inDim = rr.inDim,
      outDim = rr.outDim,
      format = q.format,
      wData = wArr
    )
  }

  implicit class BFPBlockOps(val bfp: BFPBlockQuantizer) extends AnyVal {
    def quantize(vec: DenseVector[Double]): (Array[Int], Array[Int]) =
      bfp.quantize(vec)

    def dequantize(exps: Array[Int], mants: Array[Int]): DenseVector[Double] =
      bfp.dequantize(exps, mants)
  }
}
