package org.typelevel.toolkit

import buildinfo.BuildInfo

trait PlatformSpecific {

  val platformSpecificDirectives: List[String] = List(
    s"//> using platform ${BuildInfo.platform}",
    s"//> using nativeVersion ${BuildInfo.nativeVersion}"
  )

}
