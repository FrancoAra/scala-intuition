import sbt._
import sbt.Keys._

organization in ThisBuild := "com.github.francoara"

lazy val tlConfig = Def.setting("com.typesafe" % "config" % "1.3.1")

lazy val sourcecode = Def.setting("com.lihaoyi" %% "sourcecode" % "0.1.4")

lazy val matryoshka = Def.setting("com.slamdata" %% "matryoshka-core" % "0.18.3")

lazy val discipline = Def.setting("org.typelevel" %% "discipline" % "0.8" % Test)

lazy val scalatest = Def.setting("org.scalatest" %% "scalatest" % "3.0.4"  % Test)

lazy val scalacheck = Def.setting("org.scalacheck" %% "scalacheck" % "1.13.5" % Test)

lazy val `scala-intuition` = project.in(file("."))
  .settings(moduleName := "scala-intuition", name := "Scala Intuition")
  .settings(testSettings)
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(librarySettings)

lazy val commonSettings = Seq(
  scalaVersion := "2.12.6",
  scalacOptions ++= Seq(
    "-language:implicitConversions",
    "-language:higherKinds",
    "-Ypartial-unification",
    "-Xfatal-warnings",
    "-feature"
  ),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
)

lazy val librarySettings = Seq(
  resolvers += Resolver.sonatypeRepo("releases"),
  libraryDependencies ++= tlConfig.value :: sourcecode.value :: matryoshka.value :: Nil
)

lazy val testSettings =
  Seq(libraryDependencies ++= discipline.value :: scalatest.value :: scalacheck.value :: Nil)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  homepage := Some(url("https://francoara.github.io/purity")),
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  scmInfo := Some(ScmInfo(url("https://github.com/francoara/scala-intuition"), "scm:git:git@github.com:francoara/scala-intuition.git")),
  autoAPIMappings := true,
  apiURL := Some(url("https://github.com/francoara/scala-intuition/src")),
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := version { v: String =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  }.value,
  developers := List(
    Developer(
      id = "francoara",
      name = "Francisco M. Arámburo Torres",
      email = "atfm05@gmail.com",
      url = url("https://github.com/FrancoAra")
    )
  ),
  useGpg := true
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

