package nl.pragmasoft.afanasy.service

import java.io.{IOException, InputStream}
import java.nio.file.{Files, Paths}

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import org.slf4j.LoggerFactory

import scala.annotation.tailrec

object LogbackLogging {

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  lazy val inContainer: Boolean =
    try {
      val stream = Files.lines(Paths.get("/proc/1/cgroup"))
      try {
        stream.anyMatch((line: String) => line.contains("docker"))
      } catch {
        case _: IOException => false
      } finally {
        if (stream != null) stream.close()
      }
    } catch {
      case _: Exception => false
    }

  lazy val loggingInitialised: Boolean = {

    val (configFilePath, configSource) = sys.env.get("LOGBACK_CONFIG") match {
      case Some(sysEnvValue) =>
        (sysEnvValue, "specified by environment variable LOGBACK_CONFIG")
      case None if inContainer =>
        ("logback-json.xml", "default")
      case None =>
        ("logback.xml", "default")
    }

    @tailrec
    @SuppressWarnings(Array("org.wartremover.warts.Throw"))
    def initialiseContext(configResource: InputStream, countDown: Int): Unit =
      LoggerFactory.getILoggerFactory match {
        case loggerContext: LoggerContext =>
          val configurator = new JoranConfigurator
          configurator.setContext(loggerContext)
          loggerContext.reset()
          configurator.doConfigure(configResource)
        case _ if (countDown > 0) =>
          Thread.sleep(1000)
          initialiseContext(configResource, countDown - 1)
        case _ => throw new IllegalStateException("Can't initialise logback, giving up")
      }

    Option(getClass.getClassLoader.getResourceAsStream(configFilePath)) match {
      case Some(configResource) =>
        if (!inContainer) println(s"Configuring from logback config $configFilePath ($configSource)")
        initialiseContext(configResource, 5)

      case None =>
        println(s"Logback config $configFilePath not found, stopping application")
        System.exit(1)
    }
    true
  }
}
