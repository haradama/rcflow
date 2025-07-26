package rcflow.core

import breeze.linalg.DenseVector
import rcflow.core.graph.Node

final class Concat(nodes: Seq[Node]) extends Node {

  override val outDim: Int = nodes.map(_.outDim).sum

  override def forward(x: DenseVector[Double]): DenseVector[Double] =
    DenseVector.vertcat(nodes.map(_.forward(x)): _*)

  override def reset(): Unit = nodes.foreach(_.reset())
}
