import laika.helium.config._

ThisBuild / tlBaseVersion := "0.0"
ThisBuild / startYear := Some(2023)
ThisBuild / tlSitePublishBranch := Some("main")
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))
ThisBuild / mergifyStewardConfig ~= {
  _.map(_.copy(author = "typelevel-steward[bot]"))
}

ThisBuild / crossScalaVersions := Seq("2.12.17", "2.13.10", "3.2.2")

lazy val root = tlCrossRootProject.aggregate(toolkit, docs)

lazy val toolkit = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("toolkit"))
  .settings(
    name := "toolkit",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.9.0",
      "org.typelevel" %%% "cats-effect" % "3.4.8",
      "co.fs2" %%% "fs2-io" % "3.6.1",
      "org.http4s" %%% "http4s-ember-client" % "0.23.18",
      "io.circe" %%% "circe-jawn" % "0.14.4",
      "org.http4s" %%% "http4s-circe" % "0.23.18",
      "com.monovore" %%% "decline-effect" % "2.4.1",
      "org.typelevel" %%% "munit-cats-effect" % "2.0.0-M3" // not % Test, on purpose :)
    ),
    mimaPreviousArtifacts := Set()
  )

lazy val docs = project
  .in(file("site"))
  .enablePlugins(TypelevelSitePlugin)
  .settings(
    tlSiteHelium ~= {
      _.site.mainNavigation(
        appendLinks = List(
          ThemeNavigationSection(
            "Related Projects",
            TextLink.external("https://github.com/typelevel/cats", "cats"),
            TextLink.external(
              "https://github.com/typelevel/cats-effect",
              "cats-effect"
            ),
            TextLink.external("https://github.com/typelevel/fs2", "fs2"),
            TextLink.external("https://github.com/http4s/http4s", "http4s"),
            TextLink.external("https://github.com/circe/circe", "circe"),
            TextLink.external("https://github.com/bkirwi/decline", "decline"),
            TextLink.external(
              "https://github.com/typelevel/munit-cats-effect",
              "munit-cats-effect"
            )
          )
        )
      )
    }
  )
