package rcflow.quant.api

import scala.reflect.ClassTag

object LUTGen:

  type Aux[QB] = Quantizer[Double] { type B = QB }

  def gen[QB](func: Double => Double, entries: Int, xMin: Double, xMax: Double, q: Aux[QB])(using
      ClassTag[QB]
  ): Array[QB] =
    require(entries >= 2 && xMax > xMin, "invalid LUT parameters")
    val step = (xMax - xMin) / (entries - 1)
    val lut = Array.ofDim[QB](entries)
    var i = 0
    var x = xMin
    while i < entries do
      lut(i) = q.quantize(func(x))
      x += step; i += 1
    lut

  def tanh[QB](q: Aux[QB], entries: Int = 1024, range: Double = 4.0)(using
      ClassTag[QB]
  ): Array[QB] =
    gen(math.tanh, entries, -range, range, q)

  def relu[QB](q: Aux[QB], entries: Int = 1024, range: Double = 6.0)(using
      ClassTag[QB]
  ): Array[QB] =
    val f: Double => Double = x =>
      if x < 0.0 then 0.0
      else if x > range then range
      else x
    gen(f, entries, 0.0, range, q)

  def sign[QB](q: Aux[QB])(using ClassTag[QB]): Array[QB] =
    Array(q.quantize(-1.0), q.quantize(0.0), q.quantize(1.0))
