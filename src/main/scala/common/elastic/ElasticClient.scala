package common.kafka.elastic

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch.core.{BulkRequest, BulkResponse, IndexRequest}
import co.elastic.clients.elasticsearch.core.bulk.{BulkOperation, IndexOperation}
import co.elastic.clients.elasticsearch.indices.{CreateIndexRequest, CreateIndexResponse}
import co.elastic.clients.json.JsonData
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient

import java.util.concurrent.CompletableFuture
import scala.jdk.CollectionConverters.SeqHasAsJava

case class ElasticClient( client: ElasticsearchAsyncClient, url: String ) {

  ////////////////////////////////////////////////////////////////////////////////
  // ElasticClient information
  // https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/introduction.html
  ////////////////////////////////////////////////////////////////////////////////

  def bulkIndexing[T]( idx: String, docs: Seq[T]) = {
    val ops = docs.map { d =>
      BulkOperation.of{b =>
        b.index(
          IndexOperation.of[T](i =>
            i.index(idx).document(d) ) )
      }
    }
    val req = BulkRequest.of( i => i.operations(ops.asJava) )
    client.bulk(req)
  }

  def bulkIndexingJson(idx: String, docs: Seq[String]): CompletableFuture[BulkResponse] = {
    val br = new BulkRequest.Builder()
    docs.foreach { j =>
      br.operations( (op: BulkOperation.Builder) =>
        op.index(
          IndexOperation.of[JsonData]( i =>
            i.index(idx).document(JsonData.fromJson(j)) )
        )
      )
    }
    client.bulk(br.build())
  }

  def indexing[T](idx: String, doc: T)
  = client.index(
      IndexRequest.of[T]( i => i.index(idx).document(doc) )
    )

  def indexingJson(idx: String, json: String)
  = client.index(
      IndexRequest.of[JsonData]( i =>
        i.index(idx).document( JsonData.fromJson(json))
      )
    )

  def createIndex(idx: String)
  : CompletableFuture[CreateIndexResponse] = {
      client.indices().create(
        CreateIndexRequest.of( b => b.index(idx))
    )
  }
}

object ElasticClient {
  ////////////////////////////////////////////////////////////////////////////////

  def apply( esUrl : String): ElasticClient = {

    val restClient = RestClient.builder(HttpHost.create(esUrl)).build();
    val transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    val ret = new ElasticsearchAsyncClient(transport);

    new ElasticClient(ret, esUrl)
  }
}
