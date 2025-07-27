package rcflow.core

import breeze.linalg._
import breeze.numerics.tanh
import breeze.stats.distributions.{RandBasis, Uniform}

import rcflow.core.graph

final class Reservoir(
    val size: Int,
    val spectralRadius: Double = 0.9,
    val inputScale: Double = 1.0,
    seed: Long = 42L
) extends graph.Node {

  private implicit val basis: RandBasis = RandBasis.withSeed(seed.toInt)

  private val wIn: DenseVector[Double] =
    DenseVector.rand(size, Uniform(-1.0, 1.0)) * inputScale

  private val w: DenseMatrix[Double] = {
    var raw = DenseMatrix.rand(size, size, Uniform(-1.0, 1.0))

    val maxAbsVal = raw.data.map(math.abs).max
    if (maxAbsVal > 1e-12) {
      raw = raw * (1.0 / maxAbsVal)
    }

    var v = DenseVector.rand(size, Uniform(0.0, 1.0))
    var estLambda = 0.0
    var i = 0
    while (i < 10) {
      val v2 = raw * v
      estLambda = math.sqrt(v2 dot v2)
      v = v2 /:/ (estLambda + 1e-12)
      i += 1
    }

    raw * (spectralRadius / (estLambda + 1e-12))
  }

  private var state: DenseVector[Double] = DenseVector.zeros[Double](size)

  override def reset(): Unit = {
    state := 0.0
  }

  override def outDim: Int = size

  def current: DenseVector[Double] = state

  def step(u: Double): DenseVector[Double] = {
    state = tanh((w * state) + (wIn * u))
    state
  }

  override def forward(x: DenseVector[Double]): DenseVector[Double] = {
    step(x(0))
  }

  def run(xs: Iterable[Double]): DenseMatrix[Double] = {
    val out = DenseMatrix.zeros[Double](xs.size, size)
    xs.iterator.zipWithIndex.foreach { case (u, t) =>
      out(t, ::) := step(u).t
    }
    out
  }
}

object Reservoir {
  def fromWeights(
      w: DenseMatrix[Double],
      wIn: DenseVector[Double],
      sr: Double = 0.95,
      inSc: Double = 1.0,
      seed: Long = 0L
  ): Reservoir = {
    require(w.rows == w.cols, "W must be square")
    require(w.rows == wIn.length, "wIn length mismatch")

    val r = new Reservoir(w.rows, sr, inSc, seed)

    val wf = r.getClass.getDeclaredField("w")
    wf.setAccessible(true)
    wf.set(r, w)

    val winF = r.getClass.getDeclaredField("wIn")
    winF.setAccessible(true)
    winF.set(r, wIn)

    r
  }
}
