package app

import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import common.kafka.KfConsumer.kfStream
import common.kafka.KfProducer
import common.zookeeper.{ZkClient, ZkWatcher}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer

import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Helper {

  def kafkaStreamWith(kfUrl: String, topics: Seq[String], groupName: String, opts: Opts)
                     (f: (String, String) => Unit)
                     (implicit system: ActorSystem, ec: ExecutionContext) = {

    val stream = kafkaStream(kfUrl, topics, groupName, opts)
    val go = stream.map { m => f(m.record.key, m.record.value) }
    run(go, groupName)
  }

  def kafkaStream(kfUrl: String, topics: Seq[String], groupName: String, opts: Opts)
                 (implicit system: ActorSystem) = {

    val kfConsumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(kfUrl)
      .withGroupId(groupName)
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, opts.kfOffsetReset)
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, opts.kfCommitInterval)

    kfStream(kfConsumerSettings, topics)
  }

  def run[A, B](go: Source[A, B], message: String)
               (implicit materializer: Materializer, executionContext: ExecutionContext) = {
    go.runWith(Sink.ignore)
      .onComplete {
        case Success(_) => println(s"$message : Successfully done... ")
        case Failure(err) => println(s"$message:" + err.getMessage)
      }
  }
}

////////////////////////////////////////////////////////////////////////////////
case class Controller[T]( zkClient: ZkClient, path: String)(handler: (String, Option[T]) => Option[T]) {

  val ref: AtomicReference[Option[T]] = new AtomicReference[Option[T]](None)

  def infinite: Unit = zkClient.curator.foreach(cur =>
    ZkWatcher.onZkChange(cur, path){ s =>
      val neo = handler(s, ref.get())
      ref.set(neo)
    }
  )

  def start: Controller[T] = {
    ref.set(handler("start", None))
    this
  }

  def get: Option[T] = ref.get()

}

////////////////////////////////////////////////////////////////////////////////
case class Producer( onSuccess: (String, String) => Unit,
                     onFail: (String, String) => Unit )

object Producer {

  def apply( broker: String, success: String, fail: String) = {

    lazy val p = KfProducer(broker)

    def send(topic: String)(kind: String, value: String) = p.send(new ProducerRecord[String, String](topic, kind, value))

    new Producer(
      onSuccess = send(success),
      onFail = send(fail)
    )
  }
}

