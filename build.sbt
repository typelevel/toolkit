import laika.helium.config._
import laika.rewrite.nav.{ChoiceConfig, Selections, SelectionConfig}
import java.io.File

ThisBuild / tlBaseVersion := "0.1"
ThisBuild / startYear := Some(2023)
ThisBuild / tlSitePublishBranch := Some("main")
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))
ThisBuild / mergifyStewardConfig ~= {
  _.map(_.copy(author = "typelevel-steward[bot]"))
}

ThisBuild / crossScalaVersions := Seq("2.13.12", "3.3.1")

lazy val root = tlCrossRootProject
  .aggregate(toolkit, toolkitTest, toolkitTesting)

lazy val toolkit = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("toolkit"))
  .settings(
    name := "toolkit",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.10.0",
      "org.typelevel" %%% "cats-effect" % "3.5.1",
      "co.fs2" %%% "fs2-io" % "3.9.2",
      "org.gnieh" %%% "fs2-data-csv" % "1.8.1",
      "org.gnieh" %%% "fs2-data-csv-generic" % "1.8.1",
      "org.http4s" %%% "http4s-ember-client" % "0.23.23",
      "io.circe" %%% "circe-jawn" % "0.14.6",
      "org.http4s" %%% "http4s-circe" % "0.23.23",
      "com.monovore" %%% "decline-effect" % "2.4.1"
    ),
    mimaPreviousArtifacts := Set()
  )

lazy val toolkitTest = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("toolkit-test"))
  .settings(
    name := "toolkit-test",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.10.0",
      "org.typelevel" %%% "cats-effect-testkit" % "3.5.1",
      "org.scalameta" %%% "munit" % "1.0.0-M10", // not % Test, on purpose :)
      "org.typelevel" %%% "munit-cats-effect" % "2.0.0-M3"
    ),
    mimaPreviousArtifacts := Set()
  )

lazy val toolkitTesting = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("toolkit-testing"))
  .settings(
    name := "toolkit-testing",
    scalacOptions ++= {
      if (scalaBinaryVersion.value == "2.13") Seq("-Ytasty-reader") else Nil
    },
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "munit-cats-effect" % "2.0.0-M3" % Test,
      "co.fs2" %%% "fs2-io" % "3.9.2" % Test,
      // https://github.com/VirtusLab/scala-cli/issues/2421
      "org.virtuslab.scala-cli" %% "cli" % "1.0.4" cross (CrossVersion.for2_13Use3) excludeAll (
        ExclusionRule("com.lihaoyi:geny_2.13"),
        ExclusionRule(
          "org.scala-lang.modules",
          "scala-collection-compat_2.13"
        ),
        ExclusionRule(
          "com.github.plokhotnyuk.jsoniter-scala",
          "jsoniter-scala-core_2.13"
        ),
        ExclusionRule("com.lihaoyi", "sourcecode_2.13"),
        ExclusionRule("ai.kien", "python-native-libs_2.13"),
        ExclusionRule("com.lihaoyi", "os-lib_2.13")
      )
    ),
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
    }
  )
  .jvmSettings(
    Test / test := (Test / test).dependsOn(toolkit.jvm / publishLocal).value,
    buildInfoKeys += "platform" -> "jvm"
  )
  .jsSettings(
    Test / test := (Test / test).dependsOn(toolkit.js / publishLocal).value,
    buildInfoKeys += "platform" -> "js"
  )
  .nativeSettings(
    Test / test := (Test / test).dependsOn(toolkit.native / publishLocal).value,
    buildInfoKeys += "platform" -> "native"
  )
  .enablePlugins(BuildInfoPlugin, NoPublishPlugin)

lazy val docs = project
  .in(file("site"))
  .enablePlugins(TypelevelSitePlugin)
  .dependsOn(toolkit.jvm)
  .settings(
    scalaVersion := "3.3.1",
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
