package app

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import app.Helper.{kafkaStream, run}
import common.kafka.elastic.ElasticClient

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.jdk.FutureConverters.CompletionStageOps

object Tuner {

  def main(args: Array[String]): Unit = {

    implicit lazy val system: ActorSystem = ActorSystem("DataTuner")
    val opts = Opts(args)
    JsonClarify.apply(opts)
    opts.esurl.foreach( _ => forward2ES(opts))
  }


  ////////////////////////////////////////////////////////////////////////////////
  def forward2ES(opts: Opts)(implicit system: ActorSystem) = {

    println( "==== Start Forwarding to ElasticSearch ====" )

    implicit lazy val ec: ExecutionContextExecutor = system.dispatcher
    implicit lazy val mat: Materializer = Materializer(system)

    val esProducer = ElasticClient( opts.esurl.get) // on-purpose

    val name = "forwar2es"
    val prefix = "arkFlow_"
    def indexName(key: String) = prefix + key.toLowerCase.replace(":", "_")

    val go =
      kafkaStream( opts.kfurl, Seq(opts.kfout, opts.kferr), name, opts)
        .groupedWithin(2048, 2.second)
        .mapConcat { m =>
          m.groupBy(_.record.key).flatMap { case (key, jsons) =>

            val idx = indexName(key)
            val docs = jsons.map(_.record.value)
            val inserts =
              Source.future( esProducer.bulkIndexingJson(idx, docs).asScala).completionTimeout(60.seconds)

            println(s"Forward index: ${idx}\tdocs: ${docs.size}")
            Seq(inserts)
          }
        }
        .flatMapConcat( identity)

    run(go, name)
    println( "==== Started Forwarding to ElasticSearch ====" )

  }
}





