package nl.pragmasoft.afanasy.service

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import nl.pragmasoft.afanasy.{BotConfiguration, BotService}

object Main extends IOApp {

//  val actorSystem: ActorSystem                = ActorSystem("main")
//  val materializer: Materializer              = Materializer(actorSystem)
//  val executionContext: ExecutionContext      = actorSystem.dispatcher
  lazy val config: Config                          = ConfigFactory.load()

  def run(args: List[String]): IO[ExitCode] = {

    val service =
      new BotService[IO](
        config.getConfig("bot").as[BotConfiguration])
    service.stream
      .compile
      .drain
      .as(ExitCode.Success)
  }


}
