package nl.pragmasoft.afanasy

import cats.effect.{Async, ConcurrentEffect, Timer}
import cats.implicits._
import fs2._
import nl.pragmasoft.afanasy.BotCommand._
import nl.pragmasoft.afanasy.api.{ChatId, StreamingBotAPI}
import nl.pragmasoft.afanasy.geonames.geonames.search.SearchClient

case class GeoPoint(
  geonamesId: Long,
  name: String,
  lat: BigDecimal,
  lng: BigDecimal
)

sealed trait BotCommand

object BotCommand {

  case class Introduction(chatId: ChatId) extends BotCommand
  //  case class LocationEntry(chatId: ChatId) extends BotCommand
  case class Unknown(chatId: ChatId, message: String) extends BotCommand
  case class ShowHelp(chatId: ChatId) extends BotCommand

  def fromRawMessage(chatId: ChatId, message: String): BotCommand = message match {
    case Start => Introduction(chatId)
    //    case Location  => LocationEntry(chatId)
    //    case `show` => ShowTodoList(chatId)
    //    case `clear` => ClearTodoList(chatId)
    //    case _ => AddEntry(chatId, message)
    case "?" | "/help" => ShowHelp(chatId)
    case _             => Unknown(chatId, message)
  }

  val Help = "/help"
  //  val Location = "/location"
  val Start = "/start"
}

class Bot[F[_] : ConcurrentEffect : Async : Timer](config: BotConfiguration, api: StreamingBotAPI[F], searchClient: SearchClient[F]) {

  def launch: Stream[F, Unit] = pollCommands.evalMap(handleCommand)

  private def pollCommands: Stream[F, BotCommand] = for {
    update <- api.poll(0L)
    chatIdAndMessage <- Stream.emits(update.message.flatMap(a => a.text.map(a.chat.id -> _)).toSeq)
  } yield BotCommand.fromRawMessage(chatIdAndMessage._1, chatIdAndMessage._2)

  private def handleCommand(command: BotCommand): F[Unit] = command match {

    case c: Introduction => api.send(c.chatId, List(
      s"Здравствуйте, я бот Афанасий. Я помогаю договариваться о передаче посылок. Пожалуйста, ознакомьтесь с правилами, для этого введите $Help.",
    ).mkString("\n"))

    case Unknown(chatId, word) =>
      for {
        result <- searchClient.search(word, config.geonamesUser)
        maybePlace = result.fold(_.geonames.headOption).map(item =>
          GeoPoint(item.geonameId, item.toponymName, BigDecimal(item.lat), BigDecimal(item.lng))
        )
        _ <-  api.send(chatId,
          maybePlace.map(place => s"Может быть это $place?").getOrElse(s"Я не знаю места $word")
        )
      } yield ()

    case ShowHelp(chatId) =>
      api.send(chatId, "Я пока не умею много объяснять")

  }

  //  private def clearTodoList(chatId: ChatId): F[Unit] = for {
  //    _ <- storage.clearList(chatId)
  //    _ <- logger.info(s"todo list cleared for chat $chatId") *> api.sendMessage(chatId, "Your todo-list was cleared!")
  //  } yield ()
  //
  //  private def showTodoList(chatId: ChatId): F[Unit] = for {
  //    items <- storage.getItems(chatId)
  //    _ <- logger.info(s"todo list queried for chat $chatId") *> api.sendMessage(chatId,
  //      if (items.isEmpty) "You have no tasks planned!"
  //      else ("Your todo-list:" :: "" :: items.map(" - " + _)).mkString("\n"))
  //  } yield ()
  //
  //  private def addItem(chatId: ChatId, item: Item): F[Unit] = for {
  //    _ <- storage.addItem(chatId, item)
  //    response <- F.suspend(F.catchNonFatal(Random.shuffle(List("Ok!", "Sure!", "Noted", "Certainly!")).head))
  //    _ <- logger.info(s"entry added for chat $chatId") *> api.sendMessage(chatId, response)
  //  } yield ()
}
