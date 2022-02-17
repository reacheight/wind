name := "wind"

version := "0.1"

scalaVersion := "2.13.6"

enablePlugins(JavaAppPackaging)

Compile/mainClass := Some("wind.WindApp")

val Http4sVersion = "0.23.10"
val LogbackVersion = "1.2.10"

libraryDependencies ++= Seq(
  "com.skadistats" % "clarity" % "2.7.0",
  "org.slf4j" % "slf4j-simple" % "1.7.32",
  "org.slf4j" % "slf4j-api" % "1.7.32",
  "com.softwaremill.sttp.client3" %% "core" % "3.3.16",
  "com.softwaremill.sttp.client3" %% "circe" % "3.3.16",
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1",
  "org.apache.commons" % "commons-compress" % "1.21",
  "com.lihaoyi" %% "cask" % "0.8.0",
  "org.http4s"      %% "http4s-ember-server" % Http4sVersion,
  "org.http4s"      %% "http4s-ember-client" % Http4sVersion,
  "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
  "org.http4s"      %% "http4s-circe"        % Http4sVersion,
  "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
  "ch.qos.logback"  %  "logback-classic"     % LogbackVersion         % Runtime,
  "org.scalameta"   %% "svm-subs"            % "20.2.0"
)