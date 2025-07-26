package rcflow.bench

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import rcflow.core.*

@State(Scope.Benchmark)
class ReservoirBench:
  private val res = Reservoir(200)

  @Benchmark @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def oneStep(): Unit =
    res.step(0.3)
