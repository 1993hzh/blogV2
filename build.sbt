name := """blog-v2"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  ws,
  javaJpa,
  "org.hibernate" % "hibernate-entitymanager" % "4.3.9.Final" // replace by your jpa implementation
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _)

//add junit support
libraryDependencies += "junit" % "junit" % "4.11" % "compile"
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
