name := "wind"

version := "0.1"

scalaVersion := "2.13.6"

enablePlugins(JavaAppPackaging)
enablePlugins(TypeScriptGeneratorPlugin)

Compile/mainClass := Some("wind.WindApp")

val Http4sVersion = "0.23.12"
val CirceVersion = "0.14.2"

libraryDependencies ++= Seq(
  "com.skadistats"                % "clarity"              % "2.7.1",
  "com.typesafe.scala-logging"    %% "scala-logging"       % "3.9.5",
  "ch.qos.logback"                %  "logback-classic"     % "1.2.11",
  "com.softwaremill.sttp.client3" %% "core"                % "3.3.16",
  "com.softwaremill.sttp.client3" %% "circe"               % "3.3.16",
  "io.circe"                      %% "circe-core"          % CirceVersion,
  "io.circe"                      %% "circe-generic"       % CirceVersion,
  "io.circe"                      %% "circe-parser"        % CirceVersion,
  "io.circe"                      %% "circe-bson"          % "0.5.0",
  "org.apache.commons"            % "commons-compress"     % "1.21",
  "org.http4s"                    %% "http4s-ember-server" % Http4sVersion,
  "org.http4s"                    %% "http4s-ember-client" % Http4sVersion,
  "org.http4s"                    %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"                    %% "http4s-blaze-client" % Http4sVersion,
  "org.http4s"                    %% "http4s-circe"        % Http4sVersion,
  "org.http4s"                    %% "http4s-dsl"          % Http4sVersion,
  "org.scalameta"                 %% "svm-subs"            % "20.2.0",
  "org.reactivemongo"             %% "reactivemongo"       % "1.0.10"
)