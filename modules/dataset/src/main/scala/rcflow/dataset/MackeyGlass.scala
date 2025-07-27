package rcflow.dataset

import breeze.linalg.{DenseVector, DenseMatrix}
import scala.collection.mutable.Queue
import scala.util.Random

object MackeyGlass {

  private def derivative(
      x: Double,
      xTau: Double,
      a: Double,
      b: Double,
      n: Double
  ): Double = {
    a * xTau / (1.0 + math.pow(xTau, n)) - b * x
  }

  private def rk4Step(
      xt: Double,
      xTau: Double,
      a: Double,
      b: Double,
      n: Double,
      h: Double
  ): Double = {
    val k1 = derivative(xt, xTau, a, b, n)
    val k2 = derivative(xt + 0.5 * h * k1, xTau, a, b, n)
    val k3 = derivative(xt + 0.5 * h * k2, xTau, a, b, n)
    val k4 = derivative(xt + h * k3, xTau, a, b, n)

    xt + (h / 6.0) * (k1 + 2 * k2 + 2 * k3 + k4)
  }

  def generate(
      nTimesteps: Int,
      tau: Int = 17,
      a: Double = 0.2,
      b: Double = 0.1,
      n: Double = 10.0,
      x0: Double = 1.2,
      h: Double = 1.0,
      seed: Option[Long] = Some(42L),
      history: Option[DenseVector[Double]] = None
  ): DenseVector[Double] = {

    val historyLength = math.floor(tau / h).toInt

    val rng = seed match {
      case Some(s) => new Random(s)
      case None => new Random()
    }

    val historyQueue: Queue[Double] = history match {
      case Some(histVec) =>
        require(
          histVec.length >= historyLength,
          s"length of historyVec ${histVec.length} must be at least $historyLength"
        )
        Queue(histVec.toArray.takeRight(historyLength): _*)
      case None =>
        val initialHistory = (0 until historyLength).map { _ =>
          x0 + 0.2 * (rng.nextDouble() - 0.5)
        }
        Queue(initialHistory: _*)
    }

    var xt = x0
    val result = Array.ofDim[Double](nTimesteps)

    for (i <- 0 until nTimesteps) {
      result(i) = xt

      val xTau = if (tau == 0) {
        0.0
      } else {
        val oldValue = historyQueue.dequeue()
        historyQueue.enqueue(xt)
        oldValue
      }

      xt = rk4Step(xt, xTau, a, b, n, h)
    }

    DenseVector(result)
  }

  def generateChaotic(
      nTimesteps: Int,
      seed: Option[Long] = Some(42L)
  ): DenseVector[Double] = {
    generate(
      nTimesteps = nTimesteps,
      tau = 30,
      a = 0.2,
      b = 0.1,
      n = 10.0,
      h = 0.1,
      seed = seed
    )
  }

  def generatePeriodic(
      nTimesteps: Int,
      seed: Option[Long] = Some(42L)
  ): DenseVector[Double] = {
    generate(
      nTimesteps = nTimesteps,
      tau = 13,
      a = 0.2,
      b = 0.1,
      n = 10.0,
      h = 0.1,
      seed = seed
    )
  }

  def analyzeTimeSeries(series: DenseVector[Double], name: String = "Time Series"): Unit = {
    import breeze.stats.{mean, variance}
    val mean_val = mean(series)
    val var_val = variance(series)
    val std_val = math.sqrt(var_val)
    val min_val = breeze.linalg.min(series)
    val max_val = breeze.linalg.max(series)

    println(s"=== $name ===")
    println(f"Length: ${series.length}")
    println(f"Mean: $mean_val%.6f")
    println(f"Standard Deviation: $std_val%.6f")
    println(f"Variance: $var_val%.6f")
    println(f"Min: $min_val%.6f")
    println(f"Max: $max_val%.6f")
    println(f"Range: ${max_val - min_val}%.6f")
    println()
  }
}
