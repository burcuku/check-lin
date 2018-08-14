name := "d-hitting-linearizability-checker"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.12"

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1"

libraryDependencies += "com.github.javaparser" % "javaparser-core" % "3.6.6"


cleanFiles += baseDirectory.value / "produced"
cleanFiles += baseDirectory.value / "stats"
cleanFiles += baseDirectory.value / "out"