name := """blog-v2"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  ws
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _)

//add junit support
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

//add slick support
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "1.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.1.0",
  "org.postgresql" % "postgresql" % "9.4-1204-jdbc42"
)