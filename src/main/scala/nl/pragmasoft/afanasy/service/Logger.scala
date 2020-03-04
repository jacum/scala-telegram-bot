package nl.pragmasoft.afanasy.service

trait Logger {
  import LogbackLogging._
  lazy val log: com.typesafe.scalalogging.Logger = {
    assert(loggingInitialised)
    com.typesafe.scalalogging.Logger(getClass)
  }
}
