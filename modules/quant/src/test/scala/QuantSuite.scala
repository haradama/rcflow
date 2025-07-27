package rcflow.quant

import munit.FunSuite
import rcflow.quant.impl.*
import scala.util.Random

final class QuantSuite extends FunSuite:

  test("FixedPoint round-trip ≤ 0.5 LSB") {
    val q = FixedPointQuantizer(totalBits = 8, fracBits = 4)
    val lsb = 1.0 / (1 << 4)
    val rng = new Random(0)
    (0 until 1000).foreach { _ =>
      val v = rng.between(-7.5, 7.5)
      val dq = q.dequantize(q.quantize(v))
      assert(math.abs(dq - v) <= 0.5 * lsb + 1e-12, s"v=$v dq=$dq")
    }
  }

  test("Binary quantizer outputs ±1") {
    val qs = Seq(-3.2, -0.1, 0.0, 0.5, 7.8)
    qs.foreach { v =>
      val b = BinaryWeightQuantizer.quantize(v)
      val dv = BinaryWeightQuantizer.dequantize(b)
      assert(dv == -1.0 || dv == 1.0)
      assert((v >= 0) == (dv == 1.0), s"sign mismatch v=$v dv=$dv")
    }
  }

  test("Po2 quantizer produces powers of 2") {
    val q = Po2Quantizer(expBits = 3)
    val xs = Seq(-0.7, -0.3, -0.02, 0.03, 0.5, 1.0)
    xs.foreach { v =>
      val dq = q.dequantize(q.quantize(v))
      val log2 = math.log(math.abs(dq)) / math.log(2)
      assert(log2.round == log2, s"not power-of-two: $dq")
    }
  }

  test("BFP round-trip relative error < 12.5%") {
    val q = BFPQuantizer(expBits = 5, manBits = 2)
    val rng = new Random(1)
    (0 until 500).foreach { _ =>
      val v = math.pow(2, rng.between(-5.0, 5.0)) * rng.between(-1.0, 1.0)
      if math.abs(v) < 1e-12 then ()
      else {
        val dq = q.dequantize(q.quantize(v))
        val rel = math.abs(dq - v) / math.abs(v)
        assert(rel <= 0.125 + 1e-12, s"rel=$rel v=$v dq=$dq")
      }
    }
  }

  test("MinMaxCalibrator collects extremes") {
    val c = new rcflow.quant.api.MinMaxCalibrator
    val xs = Seq(-2.0, 1.0, 0.5, 3.3, -4.1)
    xs.foreach(c.observe)
    val (mn, mx) = c.result(bits = 8)
    assertEqualsDouble(mn, -4.1, 1e-12)
    assertEqualsDouble(mx, 3.3, 1e-12)
  }
