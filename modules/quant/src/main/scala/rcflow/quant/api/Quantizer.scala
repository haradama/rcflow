package rcflow.quant.api

trait Quantizer[A] {
  type B
  def format: QFormat
  def quantize(x: A): B
  def dequantize(b: B): A
  def range: (Double, Double)
}

object IdentityQuantizer extends Quantizer[Double] {
  override type B = Double

  override val format: QFormat = QFormat.Fixed(64, 0)

  override def quantize(x: Double): Double = x

  override def dequantize(b: Double): Double = b

  override val range: (Double, Double) = (Double.NegativeInfinity, Double.PositiveInfinity)
}
