package app

import akka.actor.ActorSystem
import akka.stream.Materializer
import app.Helper.kafkaStream
import common.May.OptionMemo
import common.zookeeper.ZkClient
import transform.java.{JsonClassifier, JsonConverter, JsonValidator}
import transform.utils.JsonUtil.JValueWithPower

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.MapHasAsJava

case class JsonClarify( jc: JsonClassifier,
                        mv: Map[Int, JsonValidator],
                        mt: Map[Int, JsonConverter]) {

  def go(st: String): Either[(String, String), (String, String)] = {

    val cl = jc.search(st).ret.map( _.map(_._1).toList).left.map( "not-classified" -> _.pretty)

    def validateAndConvert(id: Int)
    : Either[(String, String), (String, String)]
    = {
      val ret = for {
        jv <- mv.get(id).toRight("not-validate" -> s"no-validate-rule for ${id}")
        jt <- mt.get(id).toRight("not-transform" -> s"no-transform-rule for ${id}")
        _  <- jv.validate(st).r.left.map( "not-validate" -> _ )
        rt <- jt.convert(st).r.left.map(  "not-transform" -> _)
      } yield {
        id.toString -> rt.pretty
      }
      ret
    }


    val ret  = cl.flatMap{ ids =>
      ids.length match {
        case 1 => validateAndConvert(ids.head)
        case n => Left("not-classified" -> ids.mkString(s"classified ($n)", ",", ""))
      }
    }

    ret
  }
}

object JsonClarify {

  import transform.utils.JsonUtil._

  def fromZookeeper(zk: ZkClient, cr: String, vr: String, tr: String)
  : Option[JsonClarify] = {
    println("fromZookeeper")

    val r0 =
      zk.get(cr).toRight(s"check $cr of zookeeper")
        .flatMap( _.toJValueOr())
        .flatMap( _.asArray())
        .map( _.children.flatMap( _.asObject0() ) )
        .map( _.flatMap( j =>
          for {
            k <- (j \ "id").asBigInt0().map( l => Integer.valueOf(l.toInt))
            v <- (j \ "rule").asString0()
          } yield k -> v
        ).toMap )
        .map ( m => JsonClassifier.apply(m.asJava))

    val r1 =
      zk.get(vr).toRight(s"check $vr of zookeeper")
        .flatMap( _.toJValueOr())
        .flatMap( _.asArray())
        .map( _.children.flatMap( _.asObject0() ) )
        .map( _.flatMap( j =>
          for {
            k <- (j \ "id").asBigInt0().map( _.toInt)
            v = (j \ "rule").pretty
          } yield k -> JsonValidator.apply(v)
        ).toMap )

    val r2 =
      zk.get(tr).toRight(s"check $tr of zookeeper")
        .flatMap(_.toJValueOr())
        .flatMap(_.asArray())
        .map(_.children.flatMap(_.asObject0()))
        .map(_.flatMap(j =>
          for {
            k <- (j \ "id").asBigInt0().map(_.toInt)
            v <- (j \ "rule").asObject0()
          } yield k -> JsonConverter.apply(v.pretty)
        ).toMap)

    val ret = for {
      _r0 <- r0
      _r1 <- r1
      _r2 <- r2
    } yield ( new JsonClarify(_r0, _r1, _r2))

    ret.left.foreach( e => println(s"not valid rule : $e"))

    ret.foreach{ r =>
      println(r.jc.show())
      r.mv.map( _._2.show()).foreach(println)
      r.mt.map( _._2.show()).foreach(println)
    }

    ret.toOption
  }

  def handler(maker: () => Option[JsonClarify])
             (command: String, m: Option[JsonClarify] )
  : Option[JsonClarify] = command.toLowerCase  match {
    case "start" | "update" =>
      maker()
        .memo(s"i got signal.. but invalid rule. check zookeeper ")
        .orElse( m)

    case o =>
      println(s"unknown signal : $o. No change is made.")
      m
  }

  ////////////////////////////////////////////////////////////////////////////////
  def apply(opts: Opts)(implicit system : ActorSystem) = {

    println("==== Start DataTuner of ArkFlow ====")

    implicit lazy val ec: ExecutionContextExecutor = system.dispatcher
    implicit lazy val mat: Materializer = Materializer(system)

    val zkClient = ZkClient(opts.zkurl)

    val jc = () => JsonClarify.fromZookeeper( zkClient,
        opts.zkClassify,
        opts.zkValidate,
        opts.zkTransform)

    val pd = Producer.apply( opts.kfurl, opts.kfout, opts.kferr )

    val controller = Controller(zkClient, opts.zkCtrl)(handler(jc)).start
    controller.infinite


    controller.get.foreach{ _ =>
      val task =
        kafkaStream(opts.kfurl, opts.kfin, opts.kfGroup, opts)
          .groupedWithin(2048, 2.second)
          .map { ms =>
            controller.get.foreach { c =>

              val (lefts, rights) = ms.map { i =>
                println(i.record.value())
                i.record.value()
              }.map(c.go).partitionMap(identity)
              val rs = rights.map { case (kind, ret) => pd.onSuccess(kind, ret) }.length
              val ls = lefts.map { case (kind, ret) => pd.onFail(kind, ret) }.length
              (ls, rs)
            }
          }
      Helper.run(task, "DataTune")
    }
    println("==== Started DataTuner of ArkFlow ====")
  }
}