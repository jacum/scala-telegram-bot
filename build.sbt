
val http4sVersion = "0.21.1"
val specs2Version = "4.8.1"
val slf4jVersion = "1.7.30"
val logbackVersion = "1.2.3"
val kindProjectorVersion = "0.11.0"
val enumeratumCirceVersion = "1.5.22"
val circeVersion = "0.12.3"
val akkaVersion = "2.6.3"
val ficusVersion = "1.4.7"

resolvers += Resolver.sbtPluginRepo("releases")

lazy val root =
  (project in file("."))

  .enablePlugins(GuardrailPlugin)
  .enablePlugins(WartRemover)
  .enablePlugins(JavaAppPackaging, DockerPlugin)

  .settings(
    organization := "nl.pragmasoft",
    name := "scala-telegram-bot",
    version := "0.0.1",
    scalaVersion := "2.13.1",
    dockerBaseImage := "openjdk:11",
    dockerUpdateLatest := true,
    dockerExposedPorts ++= Seq(8080),
    packageName in Docker := "afanasy",
    version in Docker := version.value,
    dockerRepository := Some("pragmasoftnl"),
    mainClass in Compile := Some("nl.pragmasoft.afanasy.service.Main"),
    guardrailTasks in Compile := List(
      ScalaClient(
        file("api/geonames-api.yaml"),
        pkg="nl.pragmasoft.afanasy.geonames",
        framework = "http4s",
        tracing = false)
    ),

    libraryDependencies ++= Seq(
      compilerPlugin("org.typelevel"    % "kind-projector_2.13.1" % kindProjectorVersion),

      "org.http4s"        %% "http4s-blaze-server"                  % http4sVersion,
      "org.http4s"        %% "http4s-blaze-client"                  % http4sVersion,
      "org.http4s"        %% "http4s-circe"                         % http4sVersion,
      "org.http4s"        %% "http4s-dsl"                           % http4sVersion,
      "org.http4s"        %% "http4s-prometheus-metrics"            % http4sVersion,

      "io.circe"          %% "circe-core"                           % circeVersion,
      "io.circe"          %% "circe-generic"                        % circeVersion,
      "com.iheart"        %% "ficus"                                % ficusVersion,

      "com.typesafe.akka" %% "akka-stream"                          % akkaVersion,
      "com.typesafe.akka" %% "akka-actor"                           % akkaVersion
        excludeAll ExclusionRule("org.scala-lang.modules", "scala-java8-compat_2.12"),

      "com.beachape"                %% "enumeratum-circe"           % enumeratumCirceVersion,
      "ch.qos.logback"               %  "logback-classic"           % logbackVersion,
      "com.typesafe.scala-logging"  %% "scala-logging"              % "3.9.2",

      "org.http4s"        %% "http4s-testing"            % http4sVersion % Test,
      "org.specs2"        %% "specs2-core"               % specs2Version % Test,
      "org.specs2"        %% "specs2-mock"               % specs2Version % Test,
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
