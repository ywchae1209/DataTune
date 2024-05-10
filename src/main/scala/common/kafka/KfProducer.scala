package common.kafka

import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}

import java.util.Properties

class KfProducer( brokers: String, producer: KafkaProducer[String, String]) extends Serializable with AutoCloseable {

  val callback: Callback = (m: RecordMetadata, e: Exception) =>
    if (e != null) {
      println(
        s"(topic, partition, offset): ${(m.topic, m.partition, m.offset)}\n${e.getMessage}"
      )
    }

  def sendBytes(record: ProducerRecord[String, String], callBack: Callback = callback)
  = producer.send(record, callBack)

  def send( record: ProducerRecord[String, String], callBack: Callback = callback)
  = producer.send( record, callBack)

  def close(): Unit = Option( producer).foreach ( _.close())
}

object KfProducer {

  def apply( broker: String, props: Option[Properties] = None): KfProducer = {

    // todo ::  may need some option-fitting...
    val prop = props.getOrElse {
      val  p = new Properties()
      p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
      p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
      p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
      p.put(ProducerConfig.ACKS_CONFIG, "0")
      p.put(ProducerConfig.RETRIES_CONFIG, "3")
      p.put(ProducerConfig.LINGER_MS_CONFIG, "0")
      p.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "104857600")
      //props.put(ProducerConfig.BATCH_SIZE_CONFIG, "1")
      p
    }

    val prod = new KafkaProducer[String, String](prop)
    new KfProducer( broker, prod )
  }
}
