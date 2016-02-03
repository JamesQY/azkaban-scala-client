import _root_.sbt.Keys._

name := "AzkabanClient"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-http-core-experimental" % "2.0.2",
  "org.zeroturnaround" % "zt-zip"% "1.9",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.0.2",
  "org.slf4j" % "slf4j-log4j12" %"1.6.6")

    