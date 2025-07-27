package rcflow.quant

import munit.FunSuite
import breeze.linalg.*
import rcflow.quant.impl.FixedPointQuantizer
import rcflow.quant.api.*
import QuantizerOps.*
import QuantMetrics.*

class QuantMetricsSuite extends FunSuite:

  test("SNR ≥ 40 dB for 12‑bit fixed‑point") {
    val q = FixedPointQuantizer(12, 8)
    val xs = DenseVector.tabulate(512)(i => math.sin(i * 0.01))
    val qd = q.dequantizeVec(q.quantizeVec(xs))
    val s = snr(xs, qd)
    assert(s >= 40.0, s"SNR too low: $s dB")
  }

  test("Max abs error ≤ 0.5 LSB") {
    val q = FixedPointQuantizer(10, 4)
    val lsb = 1.0 / (1 << 4)
    val xs = DenseVector.tabulate(256)(i => -7.0 + i * 0.05)
    val qd = q.dequantizeVec(q.quantizeVec(xs))
    val e = maxAbsErr(xs, qd)
    assert(e <= 0.5 * lsb + 1e-12, s"maxErr=$e")
  }
