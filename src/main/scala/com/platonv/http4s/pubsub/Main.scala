package com.platonv.http4s.pubsub

import cats.effect.{ExitCode, IO, Resource}
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import cats.implicits.*
import org.http4s.server.Router
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s.{Host, Port}
import org.http4s.server.middleware.Logger
import org.http4s.{HttpRoutes, ServerSentEvent}

object Main extends CommandIOApp("http4s-pubsub", "A simple pubsub server using http4s"):
  def routes(pubsub: PubSub) = HttpRoutes.of[IO] {
    case GET -> Root / "ping" => Ok("pong")
    case GET -> Root / "subscribe" / topic =>
      Ok(pubsub.subscribe(topic).map(s => ServerSentEvent(Some(s), None)))
    case req @ POST -> Root / "publish" / topic =>
      for {
        data <- req.as[String]
        _ <- pubsub.publish(data, topic)
        res <- Ok("published")
      } yield res
  }

  val flags = (Opts.option[String]("host", "").withDefault("0.0.0.0"),
    Opts.option[String]("port", "").withDefault("8080"))

  override def main: Opts[IO[ExitCode]] = flags.mapN { (host, port) =>
    val serverR = for {
      pubsub <- Resource.eval(PubSub.make)
      httpApp = routes(pubsub)
      withLogger = Logger.httpRoutes[IO](logHeaders = true, logBody = true)(httpApp)
       server <- EmberServerBuilder.default[IO]
                  .withHost(Host.fromString(host).get)
                  .withPort(Port.fromString(port).get)
                  .withHttpApp(withLogger.orNotFound).build
    } yield server
    serverR.use(_ => IO.never).as(ExitCode.Success)
  }
