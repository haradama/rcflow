package rcflow.examples

import breeze.linalg._
import rcflow.core._
import rcflow.core.Metrics.{rmse, r2}
import rcflow.dataset.MackeyGlass
import rcflow.quant.impl._
import rcflow.quant.api._
import rcflow.quant.api.PerChannelAffine
import scala.reflect.ClassTag

object MGFQ_PerChannel {

  private def data(): (Array[Double], Array[Double], Array[Double], Array[Double]) = {
    val ts = MackeyGlass.generate(3000, 30, 0.1, seed = Some(42L))
    val x = ts.slice(0, ts.length - 1).toArray
    val y = ts.slice(1, ts.length).toArray
    val warm = 200
    val split = ((x.length - warm) * 0.8).toInt
    (
      x.drop(warm).take(split),
      y.drop(warm).take(split),
      x.drop(warm + split),
      y.drop(warm + split)
    )
  }

  def main(args: Array[String]): Unit = {
    val (xTr, yTr, xTe, yTe) = data()

    val res = new Reservoir(100, 0.95, 0.3, seed = 3)
    val read = new RidgeReadout(100, 1, ridge = 1e-4)
    read.fit(res.run(xTr), DenseVector(yTr).toDenseMatrix.reshape(yTr.length, 1))

    val wVec = read.weights(::, 0).copy
    val scales = wVec.map(v => math.max(1e-5, math.abs(v) / 127.0))
    val zps = Array.fill(scales.length)(128)

    implicit val ct: ClassTag[Short] = ClassTag.Short
    val packed = PerChannelAffine.quantizeVec[Short](wVec, scales.data, zps, bits = 8)
    val wDeq = PerChannelAffine.dequantizeVec(packed, scales.data, zps, bits = 8)

    read.weights = DenseMatrix.create(100, 1, wDeq.data)

    val pred = TrainedModel(res, read).predict(xTe)
    val yMat = DenseVector(yTe).toDenseMatrix.reshape(yTe.length, 1)

    println("=== Per‑Channel INT8 Read‑out ===")
    printf("RMSE = %.6e ,  R² = %.4f%n", rmse(yMat, pred), r2(yMat, pred))
  }
}
