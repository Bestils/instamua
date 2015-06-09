import sbt.Keys._
import sbt._
import ExternalDependencies._



object Build extends Build {

  private val buildSettings = Defaults.coreDefaultSettings ++ Seq(
    organization := "com.htvu",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-target:jvm-1.7"),
    scalaVersion := "2.11.4",
    resolvers := RepositoryResolvers.allResolvers
  )

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = buildSettings
  ) aggregate(authentication, rest, chat)

  lazy val authentication = Project(
    id = "auth",
    base = file("auth"),
    settings = buildSettings ++ Seq(
    )
  )

  lazy val rest = Project(
    id = "rest",
    base = file("rest"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= akkaDependencies,
      libraryDependencies ++= sprayDependencies,
      libraryDependencies ++= slickDependencies,
      libraryDependencies += mysql,
      libraryDependencies += json4sNative,
      libraryDependencies += reactiveMongo,
      libraryDependencies += reactiveRedis,
      libraryDependencies += "net.ceedubs" %% "ficus" % "1.1.2"
    )
  )

  lazy val chat = Project(
    id = "chat",
    base = file("chat"),
    settings = buildSettings ++ Seq(
    )
  )

}