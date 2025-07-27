package rcflow.quant.api

sealed trait QFormat {
  def width: Int
}

object QFormat {

  case class Fixed(total: Int, frac: Int) extends QFormat {
    require(total > 0 && frac >= 0 && frac < total)
    override val width: Int = total
  }

  case object Binary extends QFormat {
    override val width: Int = 1
  }

  case class Po2(expBits: Int) extends QFormat {
    require(expBits > 0 && expBits <= 7, "expBits 1-7 supported")
    override val width: Int = 1 + expBits
  }

  case class BFP(block: Int, expBits: Int, manBits: Int) extends QFormat {
    require(block > 0 && expBits > 0 && manBits > 0)
    override val width: Int = 1 + expBits + manBits
  }

  case class Affine(bits: Int, scale: Double, zeroPoint: Int) extends QFormat {
    require(bits >= 2 && bits <= 16)
    require(scale > 0)
    override val width: Int = bits
  }
}
