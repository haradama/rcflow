import munit.FunSuite
import rcflow.core._
import rcflow.core.graph.Syntax.given
import rcflow.core.graph.Syntax.*
import breeze.linalg._

import scala.language.implicitConversions

class OnlineSuite extends FunSuite {

  test("LMS online learns y=0.5x") {
    val res = Reservoir(50, spectralRadius = 0.2, inputScale = 0.1, seed = 1)
    val lms = LMSReadout(inDim = 50, outDim = 1, alpha = 2e-3)
    res >> lms

    val n_steps = 800
    val xs = (0 until n_steps).map(i => math.sin(i * 0.1))
    xs.foreach { x =>
      val state = res.step(x)
      lms.forward(state)
      lms.train(0.5 * x)
    }

    res.reset()
    val preds = xs.map { x =>
      val s = res.step(x)
      lms.forward(s)(0)
    }

    val ys = DenseVector(xs.map(_ * 0.5).toArray).toDenseMatrix.t
    val yHat = new DenseVector(preds.toArray).toDenseMatrix.t
    val err = Metrics.nrmse(ys, yHat)
    assert(err < 0.5, s"NRMSE is too high: $err")
  }

  test("RLS online learnEvery=2 with teacher forcing") {
    val res = Reservoir(40, spectralRadius = 0.2, inputScale = 0.1, seed = 2)
    val rls = RLSReadout(inDim = 40, outDim = 1, forgetting = 1.0)
    res >> rls

    val n_steps = 800
    val xs = (0 until n_steps).map(i => math.sin(i * 0.1))
    var step = 0L
    xs.foreach { x =>
      step += 1
      val s = res.step(x)
      rls.forward(s)
      rls.train(target = 0.5 * x, learnEvery = 2, stepCnt = step)
    }

    res.reset()
    val preds = xs.map { x =>
      val s = res.step(x)
      rls.forward(s)(0)
    }

    val err = Metrics.nrmse(
      DenseVector(xs.map(_ * 0.5).toArray).toDenseMatrix.t,
      new DenseVector(preds.toArray).toDenseMatrix.t
    )
    assert(err < 0.05, s"NRMSE is too high: $err")
  }
}
