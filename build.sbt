name := "wind"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "com.skadistats" % "clarity" % "2.7.0",
  "org.slf4j" % "slf4j-simple" % "1.7.32",
  "org.slf4j" % "slf4j-api" % "1.7.32",
  "com.softwaremill.sttp.client3" %% "core" % "3.3.16",
  "com.softwaremill.sttp.client3" %% "circe" % "3.3.16",
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1",
  "org.apache.commons" % "commons-compress" % "1.21"
)