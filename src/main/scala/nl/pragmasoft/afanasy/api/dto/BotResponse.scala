package nl.pragmasoft.afanasy.api.dto

case class BotResponse[T](ok: Boolean, result: T)
