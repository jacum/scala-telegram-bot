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
  `telegram-token`: String,
  `geonames-user-id`: String,
  `geonames-api-url`: String
)

class BotService[F[_] : ConcurrentEffect : Timer](config: BotConfiguration)  {

  def stream: Stream[F, Unit] = {

    BlazeClientBuilder[F](global).stream.flatMap { httpClient =>
      val botApi = new Http4SBotAPI(config, httpClient)
      val searchClient = SearchClient[F](config.`geonames-api-url`)(implicitly[Async[F]], httpClient)
      val bot = new Bot(config, botApi, searchClient)
      bot.launch
    }
  }


}
