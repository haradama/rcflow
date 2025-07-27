package rcflow.quant.api

import scala.util.Random

object RoundClip {

  sealed trait RoundingMode
  object RoundingMode {
    case object Nearest extends RoundingMode
    case object Floor extends RoundingMode
    case object Ceil extends RoundingMode
    case object Stochastic extends RoundingMode
  }

  def round(x: Double, rndMode: RoundingMode): Long = rndMode match {
    case RoundingMode.Nearest => math.round(x)
    case RoundingMode.Floor => math.floor(x).toLong
    case RoundingMode.Ceil => math.ceil(x).toLong
    case RoundingMode.Stochastic =>
      val fl = math.floor(x)
      val p = x - fl
      val r = if (Random.nextDouble() < p) fl + 1 else fl
      r.toLong
  }

  def clip(value: Long, min: Long, max: Long): Long = {
    if (value < min) min
    else if (value > max) max
    else value
  }
}
