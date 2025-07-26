package rcflow.core

import breeze.linalg.*
import breeze.numerics.tanh
import scala.util.Random

import rcflow.core.graph

final class Reservoir(
    val size: Int,
    val spectralRadius: Double = 0.9,
    val inputScale: Double = 1.0,
    seed: Long = 42L
) extends graph.Node {

  private val rng = new Random(seed)

  private val wIn: DenseVector[Double] =
    DenseVector.fill(size) { (rng.nextDouble() * 2.0 - 1.0) * inputScale }

  private val w: DenseMatrix[Double] = {
    val raw = DenseMatrix.tabulate(size, size) { (_, _) => rng.nextDouble() * 2.0 - 1.0 }
    raw * (spectralRadius / raw.data.view.map(math.abs).max)
  }

  private var state: DenseVector[Double] = DenseVector.zeros[Double](size)
  override def reset(): Unit = state := 0.0
  override def outDim: Int = size
  def current: DenseVector[Double] = state

  def step(u: Double): DenseVector[Double] = {
    state = tanh((w * state) + (wIn * u))
    state
  }

  override def forward(x: DenseVector[Double]): DenseVector[Double] =
    step(x(0))

  def run(xs: Iterable[Double]): DenseMatrix[Double] = {
    val out = DenseMatrix.zeros[Double](xs.size, size)
    xs.iterator.zipWithIndex.foreach { case (u, t) => out(t, ::) := step(u).t }
    out
  }
}
