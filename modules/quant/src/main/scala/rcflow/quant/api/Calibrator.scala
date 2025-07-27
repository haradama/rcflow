package rcflow.quant.api

trait Calibrator {
  def observe(x: Double): Unit
  def result(bits: Int): (Double, Double)
}

final class MinMaxCalibrator extends Calibrator {
  private var mn: Double = Double.PositiveInfinity
  private var mx: Double = Double.NegativeInfinity

  def observe(x: Double): Unit = {
    if (x < mn) mn = x
    if (x > mx) mx = x
  }

  def result(bits: Int): (Double, Double) = (mn, mx)
}
