import sbt.Keys._
import sbt._
import ExternalDependencies._



object Builds extends Build {

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
  ) aggregate(authentication, user, listing, chat)

  lazy val authentication = Project(
    id = "auth",
    base = file("auth"),
    settings = buildSettings ++ Seq(
    )
  )

  lazy val user = Project(
    id = "user",
    base = file("user"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= akkaDependencies,
      libraryDependencies ++= sprayDependencies,
      libraryDependencies ++= slickDependencies,
      libraryDependencies += mysql,
      libraryDependencies += json4sNative
    )
  )

  lazy val listing = Project(
    id = "listing",
    base = file("listing"),
    settings = buildSettings ++ Seq(
    )
  )

  lazy val chat = Project(
    id = "chat",
    base = file("chat"),
    settings = buildSettings ++ Seq(
    )
  )

}