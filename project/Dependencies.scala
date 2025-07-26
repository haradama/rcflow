// project/Dependencies.scala
import sbt.*

object Dependencies {

  // ==== versions ========================================
  val breezeV = "2.2.1"
  val munitV  = "1.0.0"
  val chiselV = "3.6.0"

  // ==== modules =========================================
  val breeze = "org.scalanlp"  %% "breeze"  % breezeV
  val munit  = "org.scalameta" %% "munit"   % munitV % Test
  val chisel = "edu.berkeley.cs" %% "chisel3" % chiselV
  val chiselPlugin = compilerPlugin(
    "edu.berkeley.cs" %% "chisel3-plugin" % chiselV cross CrossVersion.full
  )
}
