package org.typelevel.toolkit

import cats.effect.IO
import munit.CatsEffectSuite

class HelloWorldSpec extends CatsEffectSuite {

  test("Hello World is hello-worlding") {
    IO.pure(true)
  }
}
