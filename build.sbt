import Dependencies._

ThisBuild / version      := "0.1.0"
ThisBuild / scalaVersion := "2.13.14"
ThisBuild / organization := "%ORGANIZATION%"

val chiselVersion = "6.5.0"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
  testFrameworks += new TestFramework("munit.Framework")
)

lazy val core = (project in file("modules/core"))
  .settings(commonSettings)
  .settings(
    name := "rcflow-core",
    libraryDependencies ++= Seq(
      "org.scalanlp"  %% "breeze"  % "2.1.0",
      "org.scalameta" %% "munit"   % "1.0.0" % Test
    )
  )

lazy val dataset = (project in file("modules/dataset"))
  .settings(commonSettings)
  .settings(
    name := "rcflow-dataset"
  )
  .dependsOn(core)

lazy val quant = (project in file("modules/quant"))
  .settings(commonSettings)
  .settings(
    name := "rcflow-quant",
    libraryDependencies ++= Seq(
      "org.scalanlp" %% "breeze" % "2.1.0",
      "org.scalameta" %% "munit" % "1.0.0" % Test
    )
  )
  .dependsOn(core) 

lazy val fixedpoint = RootProject(file("external/fixedpoint"))

lazy val chisel = project.in(file("modules/chisel"))
  .dependsOn(core, quant, fixedpoint)
  .settings(commonSettings)
  .settings(
    name := "rcflow-chisel",
    libraryDependencies ++= Seq(
      "org.chipsalliance"   %% "chisel"       % chiselVersion,
      "org.scalatest"     %% "scalatest"    % "3.2.19" % Test,
    ),
    addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-Ymacro-annotations"
    )
  )


lazy val bench = (project in file("modules/bench"))
  .settings(commonSettings)
  .settings(
    name := "rcflow-bench",
    libraryDependencies += "org.openjdk.jmh" % "jmh-core" % "1.37"
  )
  .dependsOn(core)
  .enablePlugins(JmhPlugin)

lazy val root = (project in file("."))
  .aggregate(core, quant, chisel, bench, dataset, examples)
  .settings(commonSettings)
  .settings(
    publish / skip := true
  )

lazy val examples = (project in file("examples"))
  .settings(commonSettings)
  .settings(
    name           := "rcflow-examples",
    publish / skip := true
  )
  .dependsOn(core, dataset, quant)
