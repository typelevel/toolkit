val sbtTlVersion = "0.5.0-RC2"
addSbtPlugin("org.typelevel" % "sbt-typelevel" % sbtTlVersion)
addSbtPlugin("org.typelevel" % "sbt-typelevel-mergify" % sbtTlVersion)
addSbtPlugin("org.typelevel" % "sbt-typelevel-site" % sbtTlVersion)
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.13.1")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.14")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.1")
