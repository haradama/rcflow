package rcflow.examples

import scala.language.implicitConversions
import breeze.linalg._
import breeze.stats.variance
import rcflow.core._
import rcflow.core.Metrics.{rmse, r2}
import rcflow.dataset.MackeyGlass

object MackeyGlassForecast {

  def mackeyGlass(
      tau: Int = 30,
      nSteps: Int = 3000,
      h: Double = 0.1,
      beta: Double = 0.2,
      gamma: Double = 0.1,
      n: Int = 10,
      x0: Double = 1.2
  ): DenseVector[Double] = {
    MackeyGlass.generate(
      nTimesteps = nSteps,
      tau = tau,
      a = beta,
      b = gamma,
      n = n.toDouble,
      x0 = x0,
      h = h,
      seed = Some(42L)
    )
  }

  def toForecast(
      series: DenseVector[Double],
      testRatio: Double = 0.2
  ): (Array[Double], Array[Double], Array[Double], Array[Double]) = {
    val x = series(0 until series.length - 1).toArray
    val y = series(1 until series.length).toArray

    val initialSkip = 200
    val usableX = x.drop(initialSkip)
    val usableY = y.drop(initialSkip)

    val split = (usableX.length * (1.0 - testRatio)).toInt
    val xTrain = usableX.take(split)
    val xTest = usableX.drop(split)
    val yTrain = usableY.take(split)
    val yTest = usableY.drop(split)

    (xTrain, xTest, yTrain, yTest)
  }

  def main(args: Array[String]): Unit = {
    println("=== Mackey-Glass Time Series Forecasting ===\n")

    val series = mackeyGlass()

    MackeyGlass.analyzeTimeSeries(series, "Mackey-Glass Time Series")

    val (xTrain, xTest, yTrain, yTest) = toForecast(series)
    val warmup = 100

    val reservoir = new Reservoir(size = 100, spectralRadius = 0.95, inputScale = 0.3, seed = 42L)
    val readout = new RidgeReadout(inDim = 100, outDim = 1, ridge = 1e-4)

    reservoir.reset()
    val statesAll = reservoir.run(xTrain)
    val trainStates = statesAll(warmup until statesAll.rows, ::).toDenseMatrix
    val trainTargets =
      DenseVector(yTrain.slice(warmup, yTrain.length)).toDenseMatrix
        .reshape(yTrain.length - warmup, 1)

    println(f"Train states shape: ${trainStates.rows}%d x ${trainStates.cols}%d")
    println(f"Train targets shape: ${trainTargets.rows}%d x ${trainTargets.cols}%d")
    println(f"Train targets variance: ${variance(trainTargets.toDenseVector)}%.6f")

    readout.fit(trainStates, trainTargets)
    val model = TrainedModel(reservoir, readout)

    reservoir.reset()
    val pred = model.predict(xTest)

    println(f"Test data size: ${yTest.length}%d")
    println(f"Prediction shape: ${pred.rows}%d x ${pred.cols}%d")

    val yTestMat = DenseVector(yTest).toDenseMatrix.reshape(yTest.length, 1)
    println(f"yTestMat shape: ${yTestMat.rows}%d x ${yTestMat.cols}%d")
    println(f"yTestMat variance: ${variance(yTestMat.toDenseVector)}%.6f")
    println(f"yTestMat min/max: ${min(yTestMat)}%.6e ~ ${max(yTestMat)}%.6e")
    println(f"pred min/max: ${min(pred)}%.6e ~ ${max(pred)}%.6e")

    val rmseValue = rmse(yTestMat, pred)
    val r2Value = r2(yTestMat, pred)
    println(f"RMSE = $rmseValue%.6f ,  R² = $r2Value%.5f")
  }
}
