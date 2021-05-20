name := "akka-subscription-app"

version := "1.0"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.14"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime,
  "com.github.scopt" %% "scopt" % "4.0.1",
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
  "org.json4s" %% "json4s-jackson" % "3.6.10",
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
)
