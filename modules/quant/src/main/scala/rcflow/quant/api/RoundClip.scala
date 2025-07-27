package rcflow.quant.api

import scala.util.Random

object RoundClip:

  enum RoundingMode:
    case Nearest
    case Floor
    case Ceil
    case Stochastic

  inline def round(x: Double, rndMode: RoundingMode): Long = rndMode match
    case RoundingMode.Nearest => math.round(x)
    case RoundingMode.Floor => math.floor(x).toLong
    case RoundingMode.Ceil => math.ceil(x).toLong
    case RoundingMode.Stochastic =>
      val fl = math.floor(x)
      val p = x - fl
      val r = if Random.nextDouble() < p then fl + 1 else fl
      r.toLong

  inline def clip(value: Long, min: Long, max: Long): Long =
    if value < min then min
    else if value > max then max
    else value
