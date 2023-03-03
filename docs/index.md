# typelevel-toolkit

A toolkit of **great libraries** to start building **Typelevel** apps on JVM, Node.js, and Native!

Our very own flavour of the [Scala Toolkit].

## Overview

Typelevel toolkit is a meta library that currently includes:

- [Cats] and [Cats Effect]
- [FS2] and [FS2 I/O]
- [Http4s Ember client]
- [Circe] and http4s integration
- [Decline Effect]
- [Munit Cats Effect]

Albeit being created to be used in combination with [Scala CLI], typelevel-toolkit can be imported in your `build.sbt` using:

```scala
libraryDependencies += "org.typelevel" %% "toolkit" % "@VERSION@"
// for native and js
libraryDependencies += "org.typelevel" %%% "toolkit" % "@VERSION@"
```

## Quick start

```scala mdoc
//> using lib "org.typelevel::toolkit::@VERSION@"

import cats.effect.*

object Hello extends IOApp.Simple:
  def run = IO.println("Hello toolkit!")
```

[Scala CLI]: https://scala-cli.virtuslab.org/
[Scala Toolkit]: https://github.com/VirtusLab/toolkit
[Cats]: https://typelevel.org/cats
[Cats Effect]: https://typelevel.org/cats-effect
[FS2]: https://fs2.io/#/
[FS2 I/O]: https://fs2.io/#/io
[Http4s Ember Client]: https://http4s.org/v0.23/docs/client.html
[Circe]: https://circe.github.io/circe/
[Decline Effect]: https://ben.kirw.in/decline/effect.html
[Munit Cats Effect]: https://github.com/typelevel/munit-cats-effect