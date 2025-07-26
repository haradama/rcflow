package rcflow.core.graph

import breeze.linalg.DenseVector

trait Node {
  def forward(x: DenseVector[Double]): DenseVector[Double]
  def reset(): Unit = ()
  def outDim: Int
}
