import munit.FunSuite
import rcflow.core._
import breeze.linalg._

class ObservablesSuite extends FunSuite {

  test("effectiveSpectralRadius < 1 after scaling") {
    val raw: DenseMatrix[Double] = DenseMatrix.rand[Double](50, 50)
    val rhoRaw: Double = Metrics.effectiveSpectralRadius(raw)
    val W: DenseMatrix[Double] = raw * (0.8 / rhoRaw)
    val rho: Double = Metrics.effectiveSpectralRadius(W)
    assert(rho <= 0.81)
  }

  test("memoryCapacity is positive for simple reservoir") {
    val res = new Reservoir(
      size = 100,
      spectralRadius = 0.8,
      inputScale = 0.5,
      seed = 7L
    )
    val mc: Double = Metrics.memoryCapacity(res, kMax = 50)
    assert(mc > 0.5, s"MC too low: $mc")
  }
}
