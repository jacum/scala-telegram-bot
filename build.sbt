
val http4sVersion = "0.21.1"
val specs2Version = "4.8.1"
val slf4jVersion = "1.7.30"
val logbackVersion = "1.2.3"
val kindProjectorVersion = "0.11.0"
val enumeratumCirceVersion = "1.5.22"
val circeVersion = "0.12.3"

lazy val root = (project in file("."))
  .enablePlugins(GuardrailPlugin)
  .enablePlugins(WartRemover)
  .settings(
    organization := "nl.pragmasoft",
    name := "scala-telegram-bot",
    version := "0.0.1",
    scalaVersion := "2.13.1",
    guardrailTasks in Compile := List(
      ScalaClient(
        file("api/geonames-api.yaml"),
        pkg="nl.pragmasoft.afanasy.geonames",
        framework = "http4s",
        tracing = false)
    ),
    libraryDependencies ++= Seq(
      compilerPlugin("org.typelevel"    % "kind-projector_2.13.1" % kindProjectorVersion),
      "org.http4s"      %% "http4s-blaze-server"       % http4sVersion,
      "org.http4s"      %% "http4s-blaze-client"       % http4sVersion,
      "org.http4s"      %% "http4s-circe"              % http4sVersion,
      "org.http4s"      %% "http4s-dsl"                % http4sVersion,
      "org.http4s"      %% "http4s-prometheus-metrics" % http4sVersion,
      "io.circe"        %% "circe-core"                % circeVersion,
      "io.circe"        %% "circe-generic"             % circeVersion,
      "com.beachape"    %% "enumeratum-circe"          % enumeratumCirceVersion,
      "ch.qos.logback"  %  "logback-classic"           % logbackVersion,

      "org.http4s"      %% "http4s-testing"            % http4sVersion % Test,
      "org.specs2"      %% "specs2-core"               % specs2Version % Test,
      "org.specs2"      %% "specs2-mock"               % specs2Version % Test,
    )
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings"
)
