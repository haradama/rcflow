package rcflow.quant

import munit.FunSuite
import breeze.linalg.*
import rcflow.quant.impl.*
import rcflow.quant.api.*
import rcflow.quant.api.QuantMetrics.*
import rcflow.quant.api.QuantizerOps.*
import scala.reflect.ClassTag

class AffineBFPBlockSuite extends FunSuite:

  test("Affine round-trip ≤0.5LSB") {
    val q = new AffineQuantizer(bits = 8, scale = 0.05, zeroPoint = 128)
    val lsb = 0.05
    val xs = DenseVector(-5.0, -1.25, 0.0, 2.3, 5.0)
    val qd = xs.map(q.dequantize compose q.quantize)
    assert(maxAbsErr(xs, qd) <= 0.5 * lsb + 1e-12)
  }

  test("Per-channel affine works") {
    val xs = DenseVector(0.1, -1.2, 3.4)
    val scales = Array(0.1, 0.2, 0.1)
    val zps = Array(0, 10, 20)

    given ClassTag[Short] = scala.reflect.ClassTag.Short
    val packed = PerChannelAffine.quantizeVec[Short](xs, scales, zps, bits = 8)

    val recon = DenseVector.tabulate(xs.length) { i =>
      val q = new AffineQuantizer(8, scales(i), zps(i))
      q.dequantize(packed(i))
    }

    val err = maxAbsErr(xs, recon)
    assert(err <= 0.5 * scales.max + 1e-12, s"maxErr=$err")

  }

  test("BFP block quantizer round-trip") {
    val rng = new scala.util.Random(0)
    val vec =
      DenseVector.tabulate(64)(_ => math.pow(2, rng.between(-3.0, 3.0)) * rng.between(-1.0, 1.0))
    val bfp = new BFPBlockQuantizer(block = 16, expBits = 5, manBits = 3)
    val (ex, ma) = bfp.quantize(vec)
    val recon = bfp.dequantize(ex, ma)
    val sn = QuantMetrics.snr(vec, recon)
    assert(sn >= 16.0, s"SNR too low: $sn dB")
  }
