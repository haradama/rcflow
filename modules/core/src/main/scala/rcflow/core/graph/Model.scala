package rcflow.core.graph

import breeze.linalg.DenseVector
import scala.collection.mutable.ListBuffer

final class Model private (
    private val chain: ListBuffer[Node]
) {

  def step(x: Double): DenseVector[Double] =
    chain.foldLeft(DenseVector(x))((inp, n) => n.forward(inp))

  def run(xs: Iterable[Double]): DenseVector[Double] =
    xs.foldLeft(DenseVector(0.0))((_, v) => step(v))

  def nodes: Seq[Node] = chain.toSeq
}

object Model {

  def apply(n: Node): Model = new Model(ListBuffer(n))

  def link(a: Model, b: Model): Model = {
    val buf = ListBuffer[Node]()
    buf ++= a.nodes
    buf ++= b.nodes
    new Model(buf)
  }
}
