import laika.helium.config._
import laika.rewrite.nav.{ChoiceConfig, Selections, SelectionConfig}

ThisBuild / tlBaseVersion := "0.0"
ThisBuild / startYear := Some(2023)
ThisBuild / tlSitePublishBranch := Some("main")
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))
ThisBuild / mergifyStewardConfig ~= {
  _.map(_.copy(author = "typelevel-steward[bot]"))
}

ThisBuild / crossScalaVersions := Seq("2.13.10", "3.2.2")

lazy val root = tlCrossRootProject.aggregate(toolkit, toolkitTest)

lazy val toolkit = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("toolkit"))
  .settings(
    name := "toolkit",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.9.0",
      "org.typelevel" %%% "cats-effect" % "3.5.0",
      "co.fs2" %%% "fs2-io" % "3.7.0",
      "org.gnieh" %%% "fs2-data-csv" % "1.7.1",
      "org.gnieh" %%% "fs2-data-csv-generic" % "1.7.1",
      "org.http4s" %%% "http4s-ember-client" % "0.23.19",
      "io.circe" %%% "circe-jawn" % "0.14.5",
      "org.http4s" %%% "http4s-circe" % "0.23.19",
      "com.monovore" %%% "decline-effect" % "2.4.1"
    ),
    mimaPreviousArtifacts := Set()
  )

lazy val toolkitTest = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("toolkit-test"))
  .settings(
    name := "toolkit-test",
    libraryDependencies ++= Seq(
      "org.scalameta" %%% "munit" % "1.0.0-M7", // not % Test, on purpose :)
      "org.typelevel" %%% "munit-cats-effect" % "2.0.0-M3"
    ),
    mimaPreviousArtifacts := Set()
  )

lazy val docs = project
  .in(file("site"))
  .enablePlugins(TypelevelSitePlugin)
  .dependsOn(toolkit.jvm)
  .settings(
    scalaVersion := "3.2.2",
    tlSiteHelium ~= {
      _.site.mainNavigation(
        appendLinks = List(
          ThemeNavigationSection(
            "Related Projects",
            TextLink.external("https://github.com/typelevel/fs2", "fs2"),
            TextLink.external("https://github.com/typelevel/cats", "Cats"),
            TextLink.external("https://github.com/circe/circe", "Circe"),
            TextLink.external("https://github.com/http4s/http4s", "Http4s"),
            TextLink.external("https://github.com/bkirwi/decline", "Decline"),
            TextLink.external(
              "https://github.com/typelevel/cats-effect",
              "Cats Effect"
            ),
            TextLink.external(
              "https://github.com/typelevel/munit-cats-effect",
              "Munit Cats Effect"
            )
          )
        )
      )
    },
    laikaConfig ~= {
      _.withConfigValue(
        Selections(
          SelectionConfig(
            "scala-version",
            ChoiceConfig("scala-3", "Scala 3"),
            ChoiceConfig("scala-2", "Scala 2")
          )
        )
      )
    }
  )
