import sbt.Keys._
import sbt._
import ExternalDependencies._
import com.github.bigtoast.sbtliquibase.LiquibasePlugin._
import LiquibaseConfig._

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

  //liquibase.core 3.x won't work with this plugin so use 2.0.5 for now
  //TODO: check to upgrade liquibase-core to 3.x and use newer plugin
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
      libraryDependencies += awsScala,
      libraryDependencies += "net.ceedubs" %% "ficus" % "1.1.2",
      libraryDependencies += "org.liquibase" % "liquibase-core" % "2.0.5"
    ) ++ liquibaseSettings ++ Seq (
      liquibaseUsername := liquibaseUsernameS,
      liquibasePassword := liquibasePasswordS,
      liquibaseDriver   := liquibaseDriverS,
      liquibaseUrl      := liquibaseUrlS,
      liquibaseChangelog := liquibaseChangelogS
    )
  )

  lazy val chat = Project(
    id = "chat",
    base = file("chat"),
    settings = buildSettings ++ Seq(
    )
  )
}