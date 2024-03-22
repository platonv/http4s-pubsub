ThisBuild / scalaVersion := "3.3.0"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.platonv"
ThisBuild / organizationName := "platonv"

lazy val CatsVersion = "3.5.3"
lazy val Http4sVersion = "0.23.25"
lazy val LogbackVersion = "1.4.14"

lazy val root = (project in file("."))
  .settings(
    name := "http4s-pubsub",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "org.typelevel" %% "toolkit" % "0.1.21",
      "org.typelevel" %% "toolkit-test" % "0.1.21" % Test,
      "org.http4s" %% "http4s-ember-server" % "0.23.23",
      "org.http4s" %% "http4s-dsl" % "0.23.23",
  ) )
