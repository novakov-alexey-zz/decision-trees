val scala3Version = "3.1.0"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = project
  .in(file("."))
  .settings(
    name := "decision-tree",
    version := "0.1.0-SNAPSHOT",
    run / javaOptions += "-Xmx5G",
    fork := true,
    scalaVersion := scala3Version
  )
