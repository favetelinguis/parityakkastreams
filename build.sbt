name := """parityclient"""

version := "1.0"

scalaVersion := "2.11.6"

val parityVersion = "0.5.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.4.11",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.4.11",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.paritytrading.foundation" % "foundation"    % "0.2.0",
  "com.paritytrading.parity"     % "parity-net"    % parityVersion,
  "com.paritytrading.parity"     % "parity-top"    % parityVersion,
  "com.paritytrading.parity"     % "parity-util"   % parityVersion,
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.11"
)
