package rcflow.quant.api

trait Calibrator:
  def observe(x: Double): Unit

  def result(bits: Int): (Double, Double)

final class MinMaxCalibrator extends Calibrator:
  private var mn = Double.PositiveInfinity
  private var mx = Double.NegativeInfinity
  def observe(x: Double): Unit =
    if x < mn then mn = x
    if x > mx then mx = x
  def result(bits: Int): (Double, Double) = (mn, mx)
