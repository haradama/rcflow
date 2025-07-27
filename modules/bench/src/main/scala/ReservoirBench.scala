package rcflow.bench

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

import rcflow.core._

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
class ReservoirBench {

  private var res: Reservoir = _

  @Setup(Level.Trial)
  def setup(): Unit = {
    res = new Reservoir(200)
  }

  @Benchmark
  def oneStep(bh: Blackhole): Unit = {
    bh.consume(res.step(0.3))
  }
}
