ThisBuild / tlBaseVersion := "0.0"
ThisBuild / startYear := Some(2023)

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))
ThisBuild / mergifyStewardConfig ~= {
  _.map(_.copy(author = "typelevel-steward[bot]"))
}

ThisBuild / crossScalaVersions := Seq("2.12.17", "2.13.10", "3.2.2")

lazy val root = tlCrossRootProject.aggregate(toolkit)

lazy val toolkit = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("toolkit"))
  .settings(
    name := "toolkit",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.9.0",
      "org.typelevel" %%% "cats-effect" % "3.4.8",
      "co.fs2" %%% "fs2-io" % "3.6.1",
      "org.http4s" %%% "http4s-ember-client" % "0.23.18",
      "io.circe" %%% "circe-core" % "0.14.4",
      "org.http4s" %%% "http4s-circe" % "0.23.18",
      "com.monovore" %%% "decline" % "2.4.1",
      "org.typelevel" %%% "munit-cats-effect" % "2.0.0-M3" // not % Test, on purpose :)
    ),
    mimaPreviousArtifacts := Set()
  )
