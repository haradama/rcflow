package rcflow.core

import breeze.linalg.DenseVector
import rcflow.core.graph.Node
import scala.collection.mutable.Queue

final class Delay(delay: Int, dim: Int) extends Node {
  require(delay >= 0, "delay must be ≥ 0")

  private val buf: Queue[DenseVector[Double]] =
    Queue.fill(delay)(DenseVector.zeros[Double](dim))

  private var lastOut = DenseVector.zeros[Double](dim)

  override def forward(x: DenseVector[Double]): DenseVector[Double] = {
    if (delay == 0) {
      x
    } else {
      buf.enqueue(x)
      lastOut = buf.dequeue()
      lastOut
    }
  }

  override def reset(): Unit = {
    buf.clear()
    buf ++= Seq.fill(delay)(DenseVector.zeros[Double](dim))
    lastOut := 0.0
  }

  override val outDim: Int = dim
}
