package app

import app.Opts.args2map

case class Opts(args: Array[String]) {

  val options = List(
    "kf:url",       // localhost:9094
    "kf:in",        // topic-names
    "kf:out",       // topic-name
    "kf:err",       // topic-name
    "zk:url",       // localhost:2181
    "zk:classify",  // /dataTune/classify
    "zk:validate",  // /dataTune/validate
    "zk:transform", // /dataTune/transform
    "zk:ctrl",
    "es:url"        // localhost:9200, es indices will be created by events' key of kafka
  )

  lazy val kfurl: String = get("kf:url")
  lazy val kfin: Array[String] = gets("kf:in")
  lazy val kfout: String = get("kf:out")
  lazy val kferr: String = get("kf:err")
  lazy val zkurl: String = get("zk:url")

  lazy val zkClassify = get("zk:classify")
  lazy val zkValidate = get("zk:validate")
  lazy val zkTransform = get("zk:transform")

  lazy val zkCtrl = get("zk:ctrl")

  lazy val esurl = getOption("es:url")

  ////////////////////////////////////////////////////////////////////////////////
  lazy val kfGroup = getOption("kf:group").getOrElse("DataTune")
  lazy val kfOffsetReset = getOption("kf:offset:reset").getOrElse("earliest")
  lazy val kfCommitInterval = getOption("kf:commit:interval").getOrElse("2000")

  ////////////////////////////////////////////////////////////////////////////////
  private lazy val runOptions: Map[String, Array[String]]
  = args2map(args, options.mkString("", "|", "|"))

  private def getOption(opt: String): Option[String]
  = runOptions.get(opt).flatMap(_.headOption)

  private def getOptions(opt: String): Option[Array[String]]
  = runOptions.get(opt)

  private def get(opt: String): String
  = getOption(opt).getOrElse(throw new Exception(s"${opt} must exist."))

  private def gets(opt: String): Array[String]
  = getOptions(opt).getOrElse(throw new Exception(s"${opt} must exist."))
}

object Opts {

  def args2map(args: Array[String], keywords: String)
  : Map[String, Array[String]] = {

    def toArr(args: Array[String]) = args.foldLeft(Array[Array[String]]()) {

      case (b, arg) =>
        if (arg.startsWith("-")) {
          Array(arg) +: b
        } else {
          b.headOption foreach { _ => b.update(0, b.head :+ arg) }
          b
        }
    }

    val opts = keywords.split("\\|")

    toArr(args).flatMap { arg =>
      opts
        .find(opt => arg.headOption.exists(ar => ar.dropWhile(_ == '-').equals(opt)))
        .map(_ -> arg.drop(1))
    }.toMap
  }

}