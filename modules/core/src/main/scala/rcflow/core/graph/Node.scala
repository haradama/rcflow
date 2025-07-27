package rcflow.core.graph

import breeze.linalg.DenseVector

import rcflow.core.graph.Model

trait Node {
  def forward(x: DenseVector[Double]): DenseVector[Double]
  def reset(): Unit = ()
  def outDim: Int

  def >>(other: Node): Model =
    Model.link(Model(this), Model(other))

  def &(other: Node): Model =
    Model.link(Model(this), Model(other))
}
