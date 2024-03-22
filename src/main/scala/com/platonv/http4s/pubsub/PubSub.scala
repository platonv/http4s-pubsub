package com.platonv.http4s.pubsub

import cats.effect.std.Queue
import cats.effect.{IO, Ref}
import cats.implicits.*
import fs2.Stream

final case class PubSub(ref: Ref[IO, Map[String, List[Queue[IO, String]]]]) {
  def publish(el: String, topic: String): IO[Unit] =
    ref.get.flatMap { map =>
      map.get(topic) match {
        case Some(queues) =>
          queues.traverse(_.offer(el)).void
        case None =>
          Queue.unbounded[IO, String].flatMap { queue =>
            queue.offer(el) >> ref.update(_ + (topic -> (queue :: Nil)))
          }.void
      }
    }

  def subscribe(topic: String): Stream[IO, String] = {
    val queueIO = ref.get.flatMap { map =>
      map.get(topic) match {
        case Some(queues) =>
          Queue.unbounded[IO, String].flatTap { queue =>
            ref.update(_ + (topic -> (queue :: queues)))
          }
        case None =>
          Queue.unbounded[IO, String].flatTap { queue =>
            ref.update(_ + (topic -> (queue :: Nil)))
          }
      }
    }
    Stream.eval(queueIO).flatMap { queue =>
      Stream.fromQueueUnterminated(queue).onFinalize(ref.get.flatMap { map =>
        map.get(topic) match {
          case Some(queues) =>
            ref.update(_ + (topic -> queues.filterNot(_ == queue)))
          case None =>
            IO.unit
        }
      })
    }
  }
}

object PubSub {
  def make: IO[PubSub] = Ref.of[IO, Map[String, List[Queue[IO, String]]]](Map.empty).map(PubSub(_))
}
