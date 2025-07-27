package rcflow.quant.api

trait Quantizer[A]:
  type B
  def format: QFormat
  def quantize(x: A): B
  def dequantize(b: B): A
  def range: (Double, Double)

object IdentityQuantizer extends Quantizer[Double]:
  type B = Double
  val format: QFormat = QFormat.Fixed(64, 0)
  inline def quantize(x: Double): Double = x
  inline def dequantize(b: Double): Double = b
  val range = (Double.NegativeInfinity, Double.PositiveInfinity)
