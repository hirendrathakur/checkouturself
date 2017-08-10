name := "checkouturself"

organization := "com.flipkart.fashion"

version := "0.0.1"

scalaVersion := "2.11.7"

resolvers ++= Seq(
  Resolver.jcenterRepo,
  "Maven2 Local" at Resolver.mavenLocal.root,
  "spray repo" at "http://repo.spray.io/",
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  "jBoss" at "http://repository.jboss.org/nexus/content/groups/public",
  "Akka Snapshot Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
)

/** all akka only **/
val akkaVersion = "2.4.17"
val akkaHttpVersion = "10.0.4"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion withSources() withJavadoc(),
  "com.typesafe.akka" %% "akka-stream" % akkaVersion withSources() withJavadoc(),
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion withSources(),
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion withSources() withJavadoc(),
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion withSources() withJavadoc(),
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test withSources() withJavadoc()
)

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.7",
  "commons-lang" % "commons-lang" % "2.6",
  "mysql" % "mysql-connector-java" % "5.1.37",
  "org.apache.commons" % "commons-dbcp2" % "2.1.1",
  "javax.persistence" % "persistence-api" % "1.0.2",
  "org.springframework" % "spring-jdbc" % "4.2.3.RELEASE",
  "org.bytedeco" % "javacv-platform" % "1.3.3"
)
