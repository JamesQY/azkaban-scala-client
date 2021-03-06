import _root_.sbt.Keys._

organization :="com.madhukaraphatak"

name := "azkaban-client"

version := "0.2-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-http-core" % "10.0.4",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.4",
  "org.slf4j" % "slf4j-log4j12" %"1.6.6")


pgpSecretRing := file("/home/madhu/scalapgpkeys/phatak-dev-privatekey.asc")

pgpPublicRing := file("/home/madhu/scalapgpkeys/phatak-dev-publickey.asc")

publishMavenStyle := true

publishArtifact in Test := false


publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <url>https://github.com/phatak-dev/azkaban-scala-client</url>
    <licenses>
      <license>
        <name>Apache 2.0</name>
        <url>https://github.com/phatak-dev/azkaban-scala-client/blob/master/LICENSE.txt</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:phatak-dev/java-sizeof.git</url>
      <connection>scm:git:git@github.com:phatak-dev/azkaban-scala-client.git</connection>
    </scm>
    <developers>
      <developer>
        <id>phatak-dev</id>
        <name>Madhukara phatak</name>
        <url>http://www.madhukaraphatak.com</url>
      </developer>
    </developers>)


credentials += Credentials(Path.userHome / ".sbt/0.13/" / ".credentials")


    
