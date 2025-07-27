import Dependencies._

ThisBuild / organization := "com.example"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.1"

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

lazy val quant = project
  .in(file("modules/quant"))
  .settings(
    name         := "rcflow-quant",
    libraryDependencies ++= Seq(
      "org.scalanlp" %% "breeze" % "2.1.0",
      "org.scalameta" %% "munit"   % "1.0.0" % Test
    )
  )

lazy val chisel = (project in file("modules/chisel"))
  .settings(commonSettings)
  .settings(
    name          := "rcflow-fpga",
    scalaVersion  := "2.13.12",
    libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.6.0"
  )
  .dependsOn(core, quant)

lazy val bench = (project in file("modules/bench"))
  .settings(commonSettings)
  .settings(
    name := "rcflow-bench",
    libraryDependencies += "org.openjdk.jmh" % "jmh-core" % "1.37"
  )
  .dependsOn(core)
  .enablePlugins(JmhPlugin)

lazy val root = (project in file("."))
  .aggregate(core, quant, chisel, bench, dataset, quant, examples)
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
  .dependsOn(core, dataset) 

