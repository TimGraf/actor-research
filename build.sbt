name := """actor-research"""

version := "1.0.0"

lazy val root = project in file(".")

scalaVersion := "2.11.5"

organization := "com.pjanof"

resolvers ++= Seq(
  "Maven Releases" at "http://repo.typesafe.com/typesafe/maven-releases"
)

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "io.spray"                     %%  "spray-can"              % sprayV
    , "io.spray"                   %%  "spray-routing"          % sprayV
    , "io.spray"                   %%  "spray-json"             % "1.3.1"
    , "io.spray"                   %%  "spray-testkit"          % sprayV  % "test"
    , "com.typesafe.akka"          %%  "akka-actor"             % akkaV
    , "com.typesafe.akka"          %%  "akka-testkit"           % akkaV   % "test"
    , "org.scalaz"                 %%  "scalaz-core"            % "7.1.0"
    , "com.typesafe.scala-logging" %%  "scala-logging"          % "3.1.0"
  )
}
