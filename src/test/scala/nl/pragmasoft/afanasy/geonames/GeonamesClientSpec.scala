package nl.pragmasoft.afanasy.geonames

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult

class GeonamesClientSpec extends org.specs2.mutable.Specification {

  "geocities client" should {
    "resolve city" in {

      1 must_== 1
    }
  }

//  "HelloWorld" >> {
//    "return 200" >> {
//      uriReturns200()
//    }
//    "return hello world" >> {
//      uriReturnsHelloWorld()
//    }
//  }
//
//  private[this] val retHelloWorld: Response[IO] = {
//    val getHW = Request[IO](Method.GET, uri"/hello/world")
//    val helloWorld = HelloWorld.impl[IO]
//    $name;format="Camel"$Routes.helloWorldRoutes(helloWorld).orNotFound(getHW).unsafeRunSync()
//  }
//
//  private[this] def uriReturns200(): MatchResult[Status] =
//    retHelloWorld.status must beEqualTo(Status.Ok)
//
//  private[this] def uriReturnsHelloWorld(): MatchResult[String] =
//    retHelloWorld.as[String].unsafeRunSync() must beEqualTo("{\"message\":\"Hello, world\"}")
}