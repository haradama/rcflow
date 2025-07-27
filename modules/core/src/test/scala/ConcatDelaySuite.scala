import munit.FunSuite
import scala.language.implicitConversions
import rcflow.core._
import rcflow.core.graph
import breeze.linalg._

class ConcatDelaySuite extends FunSuite {

  test("Delay 0 behaves as identity") {
    val d0 = new Delay(0, 1)
    val out = d0.forward(DenseVector(123.0))
    assertEqualsDouble(out(0), 123.0, 1e-12)
  }

  test("Delay outputs are shifted by k steps") {
    val d = new Delay(2, 1)
    val ins = Seq(10.0, 20.0, 30.0)
    val outs = ins
      .map(v => d.forward(DenseVector(v)))
      .map(_(0))
    assertEquals(outs, Seq(0.0, 0.0, 10.0))
  }

  test("Delay reset restores zeros") {
    val d = new Delay(1, 1)
    d.forward(DenseVector(5.0))
    d.reset()
    val out = d.forward(DenseVector(99.0))
    assertEqualsDouble(out(0), 0.0, 1e-12)
  }

  class ScaleNode(f: Double) extends graph.Node {
    private var last = DenseVector[Double]()
    def forward(x: DenseVector[Double]): DenseVector[Double] = {
      last = x * f
      last
    }
    override val outDim: Int = 1
    override def reset(): Unit = ()
  }

  test("Concat vertcats children outputs") {
    val n1 = new ScaleNode(2.0)
    val n2 = new ScaleNode(-1.0)
    val cat = new Concat(Seq(n1, n2))
    val out = cat.forward(DenseVector(3.0))
    assertEquals(out.length, 2)
    assertEqualsDouble(out(0), 6.0, 1e-12)
    assertEqualsDouble(out(1), -3.0, 1e-12)
  }

  test("Concat reset cascades to children") {
    val d1 = new Delay(1, 1)
    val d2 = new Delay(1, 1)
    val cat = new Concat(Seq(d1, d2))
    cat.forward(DenseVector(1.0))
    cat.reset()
    val z = cat.forward(DenseVector(42.0))
    assertEqualsDouble(z(0), 0.0, 1e-12)
    assertEqualsDouble(z(1), 0.0, 1e-12)
  }
}
