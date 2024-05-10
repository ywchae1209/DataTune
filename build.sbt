import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "DataTune"
  )

logLevel := util.Level.Warn
libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-api-scala_2.13" % "12.0",
  "org.apache.logging.log4j" % "log4j-core" % "2.19.0" % Runtime,
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.19.0"
)

excludeDependencies ++= Seq (
  "ch.qos.logback"
)
// ArkFlow
resolvers += Resolver.mavenLocal
libraryDependencies += "com.iarkdata" % "arkflow_2.13" % "0.7"

// akka stream
val AkkaVersion = "2.8.5"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" %  AkkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "4.0.2"
)

// elastic, kafka, zookeeper
libraryDependencies += "co.elastic.clients" % "elasticsearch-java" % "8.10.4"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "3.6.0"
libraryDependencies += "org.apache.zookeeper" % "zookeeper" % "3.9.1"
libraryDependencies += "org.apache.curator" % "curator-framework" % "5.5.0"
