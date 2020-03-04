package nl.pragmasoft.afanasy.api

import cats.effect.{Effect, Sync}
import cats.implicits._
import fs2.Stream
import io.circe.generic.auto._
import nl.pragmasoft.afanasy.{BotConfiguration, Logging}
import nl.pragmasoft.afanasy.api.dto.{BotResponse, BotUpdate}
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.{EntityDecoder, Uri}

trait BotAPI[F[_], S[_]] {
  def send(chatId: ChatId, message: String): F[Unit]
  def poll(fromOffset: Offset): S[BotUpdate]
}

trait StreamingBotAPI[F[_]] extends BotAPI[F, Stream[F, *]]

class Http4SBotAPI[F[_]](
  config: BotConfiguration,
  client: Client[F])(implicit F: Sync[F]) extends StreamingBotAPI[F] with Logging {

  implicit val decoder: EntityDecoder[F, BotResponse[List[BotUpdate]]] = jsonOf[F, BotResponse[List[BotUpdate]]]

  private val botApiUri: Uri = uri"""https://api.telegram.org""" / s"bot${config.token}"

  def send(chatId: ChatId, message: String): F[Unit] = {

    // safely build a uri to query
    val uri = botApiUri / "sendMessage" =? Map(
      "chat_id" -> List(chatId.toString),
      "parse_mode" -> List("Markdown"),
      "text" -> List(message)
    )

    client.expect[Unit](uri)
  }

  def poll(fromOffset: Offset): Stream[F, BotUpdate] =
    Stream(()).repeat.covary[F]
      .evalMapAccumulate(fromOffset) { case (offset, _) => requestUpdates(offset) }
      .flatMap { case (_, response) => Stream.emits(response.result) }

  private def requestUpdates(offset: Offset): F[(Offset, BotResponse[List[BotUpdate]])] = {

    val uri = botApiUri / "getUpdates" =? Map(
      "offset" -> List((offset + 1).toString),
      "timeout" -> List("0.5"), // timeout to throttle the polling
      "allowed_updates" -> List("""["message"]""")
    )

    client.expect[BotResponse[List[BotUpdate]]](uri)
      .map(response =>
        (lastOffset(response).getOrElse(offset), response))
      .recoverWith {
        case ex =>
          logger.error(ex)("Failed to poll updates")
          F.delay(offset -> BotResponse(ok = true, Nil))
      }
  }

  // just get the maximum id out of all received updates
  private def lastOffset(response: BotResponse[List[BotUpdate]]): Option[Offset] =
    response.result match {
      case Nil => None
      case nonEmpty => Some(nonEmpty.maxBy(_.update_id).update_id)
    }
}
