import munit.FunSuite
import breeze.linalg._
import rcflow.core._
import scala.language.implicitConversions

class OnlineSuite extends FunSuite {

  test("LMS online learns y=0.5x") {
    val res = new Reservoir(
      size = 50,
      spectralRadius = 0.2,
      inputScale = 0.1,
      seed = 1L
    )
    val lms = new LMSReadout(inDim = 50, outDim = 1, alpha = 2e-3)
    res >> lms

    val nSteps = 800
    val xs: Seq[Double] = (0 until nSteps).map(i => math.sin(i * 0.1))
    xs.foreach { x =>
      val state = res.step(x)
      lms.forward(state)
      lms.train(target = 0.5 * x, learnEvery = 1, stepCnt = 0L) // learnEvery default=1
    }

    res.reset()
    val preds: Seq[Double] = xs.map { x =>
      val s = res.step(x)
      lms.forward(s)(0)
    }

    val ysMat = new DenseVector(xs.map(_ * 0.5).toArray).toDenseMatrix.t
    val yHatMat = new DenseVector(preds.toArray).toDenseMatrix.t
    val err = Metrics.nrmse(ysMat, yHatMat)
    assert(err < 0.5, s"NRMSE is too high: $err")
  }

  test("RLS online learnEvery=2 with teacher forcing") {
    val res = new Reservoir(
      size = 40,
      spectralRadius = 0.2,
      inputScale = 0.1,
      seed = 2L
    )
    val rls = new RLSReadout(inDim = 40, outDim = 1, forgetting = 1.0)
    res >> rls

    val nSteps = 800
    val xs: Seq[Double] = (0 until nSteps).map(i => math.sin(i * 0.1))
    var stepCnt = 0L
    xs.foreach { x =>
      stepCnt += 1
      val s = res.step(x)
      rls.forward(s)
      rls.train(target = 0.5 * x, learnEvery = 2, stepCnt = stepCnt)
    }

    res.reset()
    val preds: Seq[Double] = xs.map { x =>
      val s = res.step(x)
      rls.forward(s)(0)
    }

    val ysMat = new DenseVector(xs.map(_ * 0.5).toArray).toDenseMatrix.t
    val yHatMat = new DenseVector(preds.toArray).toDenseMatrix.t
    val err = Metrics.nrmse(ysMat, yHatMat)
    assert(err < 0.05, s"NRMSE is too high: $err")
  }
}
