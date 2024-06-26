ThisBuild / scalaVersion := "2.13.8"

ThisBuild / version := "1.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(TypeScriptGeneratorPlugin)
  .settings(
    name := """windota""",
    libraryDependencies ++= Seq(
      guice,
      openId,
      "ch.qos.logback" % "logback-classic" % "1.3.5",
      "com.typesafe.scala-logging"    %% "scala-logging"       % "3.9.5",
      "com.softwaremill.sttp.client3" %% "core"                % "3.7.1",
      "com.softwaremill.sttp.client3" %% "circe"               % "3.7.1",
      "io.circe"                      %% "circe-core"          % "0.14.2",
      "io.circe"                      %% "circe-generic"       % "0.14.2",
      "io.circe"                      %% "circe-parser"        % "0.14.2",
      "io.circe"                      %% "circe-bson"          % "0.5.0",
      "com.skadistats"                % "clarity"              % "2.7.9",
      "org.apache.commons"            % "commons-compress"     % "1.21",
      "org.reactivemongo"             %% "reactivemongo"       % "1.1.0-RC10",
      "com.dripower"                  %% "play-circe"          % "2814.2"
    )
  )