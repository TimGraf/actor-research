name := """actor-research"""

version := "1.1"

lazy val root = project in file(".")

scalaVersion := "2.11.7"

organization := "com.pjanof"

resolvers ++= Seq(
  "Maven Releases" at "http://repo.typesafe.com/typesafe/maven-releases"
)

libraryDependencies ++= {
  val akkaV = "2.3.12"
  val sprayV = "1.3.3"
  Seq(
    "io.spray"                     %%  "spray-can"              % sprayV
    , "io.spray"                   %%  "spray-routing"          % sprayV
    , "io.spray"                   %%  "spray-json"             % "1.3.1"
    , "io.spray"                   %%  "spray-testkit"          % sprayV  % "test"
    , "com.typesafe.akka"          %%  "akka-actor"             % akkaV
    , "com.typesafe.akka"          %%  "akka-testkit"           % akkaV   % "test"
    , "com.typesafe.akka"          %%  "akka-slf4j"             % akkaV
    , "org.scalaz"                 %%  "scalaz-core"            % "7.1.0"
    , "ch.qos.logback"              %  "logback-classic"        % "1.1.3"
    , "com.typesafe.scala-logging" %%  "scala-logging"          % "3.1.0"
  )
}

// continuous build
Revolver.settings

// run options
javaOptions in run ++= Seq(
  "-Dconfig.file=src/main/resources/application.conf",
  "-Dlogback.configurationFile=src/main/resources/logback.xml"
)

scalacOptions ++= Seq("-unchecked", "-feature")
