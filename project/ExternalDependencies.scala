import sbt._

object ExternalDependencies {
  val akkaVersion = "2.3.2"
  val akkaDependencies = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion exclude ("org.scala-lang" , "scala-library"),
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion exclude ("org.slf4j", "slf4j-api")
  )

  val json4sJackson = "org.json4s" %% "json4s-jackson" % "3.2.11"

  val sprayVersion = "1.3.1"
  val sprayDependencies = Seq(
    "io.spray" %% "spray-can" % sprayVersion,
    "io.spray" %% "spray-http" % sprayVersion,
    "io.spray" %% "spray-routing" % sprayVersion,
    "io.spray" %% "spray-json" % "1.3.2" exclude ("org.scala-lang" , "scala-library")
  )

  val slickDependencies = Seq(
    "com.typesafe.slick" %% "slick" % "3.0.0",
    "joda-time" % "joda-time" % "2.7",
    "org.joda" % "joda-convert" % "1.7",
    "com.github.tototoshi" %% "slick-joda-mapper" % "2.0.0",
    "com.zaxxer" % "HikariCP-java6" % "2.3.3"
  )

  val mysql = "mysql" % "mysql-connector-java" % "5.1.35"
}
