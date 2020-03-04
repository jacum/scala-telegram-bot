package nl.pragmasoft.afanasy.api.dto

case class BotMessage(
  message_id: Long,
  chat: Chat,
  text: Option[String])
