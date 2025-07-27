package rcflow.examples

import breeze.linalg.*
import rcflow.core.*
import rcflow.core.Metrics.{rmse, r2}
import rcflow.dataset.MackeyGlass
import rcflow.quant.impl.*
import rcflow.quant.api.*
import rcflow.quant.api.QFlow
import rcflow.quant.api.QuantizerOps.*

object MGFQ_Po2:

  private def data() =
    val ts = MackeyGlass.generate(3000, 30, 0.1, seed = Some(42L))
    val x = ts(0 until ts.length - 1).toArray
    val y = ts(1 until ts.length).toArray
    val warm = 200
    val split = ((x.length - warm) * 0.8).toInt
    (x.drop(warm).take(split), y.drop(warm).take(split), x.drop(warm + split), y.drop(warm + split))

  def main(args: Array[String]): Unit =
    val (xTr, yTr, xTe, yTe) = data()

    val resFP = Reservoir(100, 0.95, 0.3, seed = 2)
    val qRes = QFlow.quantize(resFP, Po2Quantizer(5), FixedPointQuantizer(8, 6))

    val Wq = Po2Quantizer(5).dequantizeMat(qRes.wData, 100, 100)
    val Win = FixedPointQuantizer(8, 6).dequantizeVec(qRes.wInData)
    val Wn = normalizeSpectralRadius(Wq, 0.95)

    val resQ = Reservoir.fromWeights(Wn, Win, sr = 0.95, inSc = 0.3, seed = 2)

    val read = RidgeReadout(100, 1, ridge = 1e-4)
    read.fit(resQ.run(xTr), DenseVector(yTr).toDenseMatrix.reshape(yTr.length, 1))

    val pred = TrainedModel(resQ, read).predict(xTe)
    val yMat = DenseVector(yTe).toDenseMatrix.reshape(yTe.length, 1)
    println("=== Po2‑5bit Reservoir (FP32 Read‑out) ===")
    printf("RMSE = %.6e ,  R² = %.4f%n", rmse(yMat, pred), r2(yMat, pred))
