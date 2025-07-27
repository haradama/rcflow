package rcflow.examples

import breeze.linalg._
import rcflow.core._
import rcflow.core.Metrics.{rmse, r2}
import rcflow.dataset.MackeyGlass
import rcflow.quant.impl._
import rcflow.quant.api._
import rcflow.quant.api.QFlow
import rcflow.quant.api.QuantizerOps._
import scala.reflect.ClassTag

object MGFQ_Relearn {

  private def dataset(): (Array[Double], Array[Double], Array[Double], Array[Double]) = {
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
    val (xTr, yTr, xTe, yTe) = dataset()

    val resFP = new Reservoir(100, 0.95, 0.3, seed = 1)
    val qRes = QFlow.quantize(resFP, BinaryWeightQuantizer, new FixedPointQuantizer(8, 6))

    val Wbin = BinaryWeightQuantizer.dequantizeMat(qRes.wData, 100, 100)
    val Win = new FixedPointQuantizer(8, 6).dequantizeVec(qRes.wInData)
    val Wn = normalizeSpectralRadius(Wbin, 0.95)

    val resQ = Reservoir.fromWeights(Wn, Win, sr = 0.95, inSc = 0.3, seed = 1)

    val read = new RidgeReadout(100, 1, ridge = 1e-4)
    read.fit(resQ.run(xTr), DenseVector(yTr).toDenseMatrix.reshape(yTr.length, 1))

    val maxAbs = read.weights.data.map(math.abs).max
    val qOut = AffineQuantizer.fromMaxAbs(bits = 8, maxAbs)

    implicit val ct: ClassTag[Short] = ClassTag.Short
    val qRd = QFlow.quantize(read, qOut)
    read.weights = qOut.dequantizeMat(qRd.wData, 100, 1)

    val pred = TrainedModel(resQ, read).predict(xTe)
    val yMat = DenseVector(yTe).toDenseMatrix.reshape(yTe.length, 1)
    println("=== Binary‑1bit Reservoir + Re‑learn Read‑out ===")
    printf("RMSE = %.6e ,  R² = %.4f%n", rmse(yMat, pred), r2(yMat, pred))
  }
}
