name := "urlshortener"

version := "1.0"

lazy val `urlshortener` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(

  "com.kenshoo" %% "metrics-play" % "2.6.6_0.6.2",

  "org.webjars" %% "webjars-play" % "2.7.3",
  "org.webjars" % "swagger-ui" % "3.9.0",
  "io.swagger" %% "swagger-play2" % "1.6.0",

  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,

  jdbc, ehcache, ws, specs2 % Test, guice
)
