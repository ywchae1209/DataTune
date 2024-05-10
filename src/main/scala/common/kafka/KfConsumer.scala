package common.kafka

import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object KfConsumer {

  ////////////////////////////////////////////////////////////////////////////////
  // Kafka consuming to stream
  ////////////////////////////////////////////////////////////////////////////////
  def kfStream[K, V](settings: ConsumerSettings[K, V], topics: Seq[String])
  : Source[CommittableMessage[K, V], Consumer.Control]
  = Consumer.committableSource(settings, Subscriptions.topics(topics: _*))

  def kfStreamsForeach[K, V](settings: ConsumerSettings[K, V], topics: Seq[String],
                            bulk: Int = 1024, within: FiniteDuration = 1.second )
                           (f: Seq[CommittableMessage[K, V]] => Unit)( implicit materializer: Materializer)

  = kfStream(settings, topics)
    .groupedWithin(bulk, within)
    .map(f)
    .runWith(Sink.ignore)

  def kfStreamForeach[K, V](settings: ConsumerSettings[K, V], topics: Seq[String],
                            bulk: Int = 1024, within: FiniteDuration = 1.second)
                           (f: CommittableMessage[K, V] => Unit)(implicit materializer: Materializer)

  = kfStream(settings, topics)
    .map(f)
    .runWith(Sink.ignore)
}
