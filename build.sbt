import laika.helium.config._
import laika.config.{ChoiceConfig, Selections, SelectionConfig}
import java.io.File

ThisBuild / tlBaseVersion := "0.1"
ThisBuild / startYear := Some(2023)
ThisBuild / tlSitePublishBranch := Some("main")
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))
ThisBuild / mergifyStewardConfig ~= {
  _.map(_.withAuthor("typelevel-steward[bot]"))
}

ThisBuild / crossScalaVersions := Seq("2.13.15", "3.3.3")

lazy val root = tlCrossRootProject
  .aggregate(toolkit, toolkitTest, tests)

lazy val toolkit = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("toolkit"))
  .settings(
    name := "toolkit",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.11.0",
      "org.typelevel" %%% "cats-effect" % "3.5.4",
      "co.fs2" %%% "fs2-io" % "3.11.0",
      "org.gnieh" %%% "fs2-data-csv" % "1.11.1",
      "org.gnieh" %%% "fs2-data-csv-generic" % "1.11.1",
      "org.http4s" %%% "http4s-ember-client" % "0.23.28",
      "io.circe" %%% "circe-jawn" % "0.14.8",
      "org.http4s" %%% "http4s-circe" % "0.23.28",
      "com.monovore" %%% "decline-effect" % "2.4.1"
    ),
    mimaPreviousArtifacts := Set()
  )

lazy val toolkitTest = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("toolkit-test"))
  .settings(
    name := "toolkit-test",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.11.0",
      "org.typelevel" %%% "cats-effect-testkit" % "3.5.4",
      "org.scalameta" %%% "munit" % "1.0.0", // not % Test, on purpose :)
      "org.typelevel" %%% "munit-cats-effect" % "2.0.0"
    ),
    mimaPreviousArtifacts := Set()
  )

lazy val tests = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("tests"))
  .settings(
    name := "tests",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "munit-cats-effect" % "2.0.0" % Test,
      "co.fs2" %%% "fs2-io" % "3.11.0" % Test,
      "org.virtuslab.scala-cli" %% "cli" % "1.4.0" cross (CrossVersion.for2_13Use3)
    ),
    buildInfoKeys += scalaBinaryVersion,
    buildInfoKeys += "nativeVersion" -> nativeVersion,
    buildInfoKeys += BuildInfoKey.map(Compile / dependencyClasspath) {
      case (_, v) =>
        "classPath" -> v.seq
          .map(_.data.getAbsolutePath)
          .mkString(File.pathSeparator)
    },
    buildInfoKeys += BuildInfoKey.action("javaHome") {
      val path = sys.env.get("JAVA_HOME").orElse(sys.props.get("java.home")).get
      if (path.endsWith("/jre")) {
        // handle JDK 8 installations
        path.replace("/jre", "")
      } else path
    },
    buildInfoKeys += "scala3" -> (scalaBinaryVersion.value == "3")
  )
  .jvmSettings(
    Test / test := (Test / test)
      .dependsOn(toolkit.jvm / publishLocal, toolkitTest.jvm / publishLocal)
      .value,
    buildInfoKeys += "platform" -> "jvm"
  )
  .jsSettings(
    Test / test := (Test / test)
      .dependsOn(toolkit.js / publishLocal, toolkitTest.js / publishLocal)
      .value,
    buildInfoKeys += "platform" -> "js",
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
  .nativeSettings(
    Test / test := (Test / test)
      .dependsOn(
        toolkit.native / publishLocal,
        toolkitTest.native / publishLocal
      )
      .value,
    buildInfoKeys += "platform" -> "native"
  )
  .enablePlugins(BuildInfoPlugin, NoPublishPlugin)

lazy val docs = project
  .in(file("site"))
  .enablePlugins(TypelevelSitePlugin)
  .dependsOn(toolkit.jvm)
  .settings(
    scalaVersion := "3.3.3",
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
