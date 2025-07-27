import munit.FunSuite
import rcflow.core.*
import rcflow.core.graph.Syntax.given
import breeze.linalg.*

class ConcatDelaySuite extends FunSuite {
  test("Delay 0 behaves as identity") {
    val d0 = Delay(0, 1)
    val out = d0.forward(DenseVector(123.0))
    assertEqualsDouble(out(0), 123.0, 1e-12)
  }

  test("Delay outputs are shifted by k steps") {
    val d = Delay(delay = 2, dim = 1)

    val ins = Seq(10.0, 20.0, 30.0)
    val outs = ins
      .map { v =>
        d.forward(DenseVector(v))
      }
      .map(_(0))

    assertEquals(outs, Seq(0.0, 0.0, 10.0))
  }

  test("Delay reset restores zeros") {
    val d = Delay(1, 1)
    d.forward(DenseVector(5.0))
    d.reset()
    val out = d.forward(DenseVector(99.0))
    assertEqualsDouble(out(0), 0.0, 1e-12)
  }

  class ScaleNode(f: Double) extends graph.Node {
    private var last = DenseVector[Double]()
    def forward(x: DenseVector[Double]): DenseVector[Double] = {
      last = x * f; last
    }
    val outDim: Int = 1
    override def reset(): Unit = ()
  }

  test("Concat vertcats children outputs") {
    val n1 = ScaleNode(2.0)
    val n2 = ScaleNode(-1.0)
    val cat = Concat(Seq(n1, n2))
    val out = cat.forward(DenseVector(3.0))

    assertEquals(out.length, 2)
    assertEqualsDouble(out(0), 6.0, 1e-12)
    assertEqualsDouble(out(1), -3.0, 1e-12)
  }

  test("Concat reset cascades to children") {
    val d1 = Delay(1, 1)
    val d2 = Delay(1, 1)
    val cat = Concat(Seq(d1, d2))

    cat.forward(DenseVector(1.0))
    cat.reset()
    val z = cat.forward(DenseVector(42.0))
    assertEqualsDouble(z(0), 0.0, 1e-12)
    assertEqualsDouble(z(1), 0.0, 1e-12)
  }
}
