import munit.FunSuite
import rcflow.core.*
import breeze.linalg.*

class ObservablesSuite extends FunSuite {

  test("effectiveSpectralRadius < 1 after scaling") {
    val raw = DenseMatrix.rand[Double](50, 50)
    val rhoRaw = Metrics.effectiveSpectralRadius(raw)
    val W = raw * (0.8 / rhoRaw)
    val rho = Metrics.effectiveSpectralRadius(W)
    assert(rho <= 0.81)
  }

  test("memoryCapacity is positive for simple reservoir") {
    val res = Reservoir(100, spectralRadius = 0.8, inputScale = 0.5, seed = 7)
    val mc = Metrics.memoryCapacity(res, kMax = 50)
    assert(mc > 0.5, s"MC too low: $mc")
  }
}
