import sbt._

object ExternalDependencies {
  val akkaVersion = "2.3.2"
  val akkaDependencies = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion exclude ("org.scala-lang" , "scala-library"),
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion exclude ("org.slf4j", "slf4j-api")
  )

  val json4sNative = "org.json4s" %% "json4s-native" % "3.2.11"

  val sprayVersion = "1.3.1"
  val sprayDependencies = Seq(
    "io.spray" %% "spray-can" % sprayVersion,
    "io.spray" %% "spray-http" % sprayVersion,
    "io.spray" %% "spray-routing" % sprayVersion,
    "io.spray" %%  "spray-json" % sprayVersion,
    "io.spray" %% "spray-testkit" % sprayVersion
  )

  val slickDependencies = Seq(
    "com.typesafe.slick" %% "slick" % "3.0.0",
    "joda-time" % "joda-time" % "2.7",
    "org.joda" % "joda-convert" % "1.7",
    "com.github.tototoshi" %% "slick-joda-mapper" % "2.0.0",
    "com.zaxxer" % "HikariCP-java6" % "2.3.3",
    "org.specs2" %% "specs2" % "2.3.13"
  )

  val reactiveMongo = "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23"
  val reactiveRedis = "net.debasishg" %% "redisreact" % "0.7"

  val mysql = "mysql" % "mysql-connector-java" % "5.1.35"

  val awsScala = "com.github.seratch" %% "awscala" % "0.5.+"

  val loggingDependencies = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "ch.qos.logback" % "logback-classic" % "1.1.2"
  )

  val imageMagick = "org.im4java" % "im4java" % "1.4.0"
}
