package nl.pragmasoft.afanasy

import cats.effect._
import cats.implicits._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.Http4sClientDsl
import fs2.Stream
import nl.pragmasoft.afanasy.api.Http4SBotAPI
import nl.pragmasoft.afanasy.api.dto.{BotResponse, BotUpdate}
import nl.pragmasoft.afanasy.geonames.geonames.search.SearchClient
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

import scala.concurrent.ExecutionContext.global

case class BotConfiguration(
  token: String,
  geonamesUser: String,
  geonamesHost: String
)

object App extends IOApp with Http4sClientDsl[IO] {

  override def run(args: List[String]): IO[ExitCode] = {

    // todo read from application.conf
    val config = BotConfiguration(
      "1072932428:AAFVnz8bsiSm0IIrC7x4eS7Ufi_LR29Y90g",
      "tim_pragmasoft",
      "http://api.geonames.org"
    )
    process[IO](config)
      .compile
      .drain
      .as(ExitCode.Success)
  }

  def process[F[_] : ConcurrentEffect : Async : Timer](config: BotConfiguration): Stream[F, Unit] = {

    implicit val clock: Clock[F] = Clock.create[F]

    BlazeClientBuilder[F](global).stream.flatMap { httpClient =>
      val botApi = new Http4SBotAPI(config, httpClient)
      val searchClient = SearchClient[F](config.geonamesHost)(implicitly[Async[F]], httpClient)
      val bot = new Bot(config, botApi, searchClient)
      bot.launch
    }
  }


}
