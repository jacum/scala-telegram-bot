package nl.pragmasoft.afanasy

import org.log4s.Logger

trait Logging {
  protected[this] lazy val logger: Logger = org.log4s.getLogger
}
