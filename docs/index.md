# typelevel-toolkit

A toolkit of **great libraries** to start building **Typelevel** apps on JVM, Node.js, and Native!

Our very own flavour of the [Scala Toolkit].

## Overview

Typelevel toolkit is a meta library that currently includes these libraries:

- [Cats] and [Cats Effect]
- [fs2] and [fs2 I/O]
- [fs2 data csv] and its generic module
- [Http4s Ember client]
- [Circe] and http4s integration
- [Decline Effect]
- [Munit Cats Effect]

and it's published for Scala 2.12, 2.13 and 3.2.2.

To use it with [Scala CLI] use this directive:
```scala
//> using toolkit typelevel:latest
```

Albeit being created to be used with [Scala CLI], typelevel-toolkit can be imported into your `build.sbt` using:
```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "toolkit" % "@VERSION@",
  "org.typelevel" %% "toolkit-test" % "@VERSION@" % Test
)

// use %%% for Scala.js and Scala Native
libraryDependencies ++= Seq(
  "org.typelevel" %%% "toolkit" % "@VERSION@",
  "org.typelevel" %%% "toolkit-test" % "@VERSION@" % Test
)
```

## Quick Start

Getting started with the toolkit is as simple as running a single command:

```sh
scala-cli --power new typelevel/toolkit.g8
```

## Example

@:select(scala-version)
@:choice(scala-3)
```scala mdoc:reset:silent
//> using toolkit typelevel:latest

import cats.effect.*

object Hello extends IOApp.Simple:
  def run = IO.println("Hello toolkit!")
```
@:choice(scala-2)
```scala mdoc:reset:silent
//> using toolkit typelevel:latest

import cats.effect._

object Hello extends IOApp.Simple {
  def run = IO.println("Hello toolkit!")
}
```
@:@

### Scala Native usage

Since the native flavour of most of the **Typelevel libraries** is, at the moment, built against Scala Native 0.4.15, when using [Scala CLI] you have to declare the Scala native's version, i.e.

@:select(scala-version)
@:choice(scala-3)
```scala mdoc:reset:silent
//> using toolkit typelevel:latest
//> using platform native
//> using nativeVersion 0.4.15

import cats.effect.*

object Hello extends IOApp.Simple:
  def run = IO.println("Hello toolkit!")
```
@:choice(scala-2)
```scala mdoc:reset:silent
//> using toolkit typelevel:latest
//> using platform native
//> using nativeVersion 0.4.15

import cats.effect._

object Hello extends IOApp.Simple {
  def run = IO.println("Hello toolkit!")
}
```
@:@

## Native Image

When building GraalVM Native Image the `--no-fallback` option is required, otherwise native-image will try searching
a static reflection configuration for [this Enumeration method]. Thus using this flag is safe only if you're not using
Enumerations in your codebase, see [this comment] for more info.

[Scala CLI]: https://scala-cli.virtuslab.org
[Scala Toolkit]: https://github.com/VirtusLab/toolkit
[Cats]: https://typelevel.org/cats
[Cats Effect]: https://typelevel.org/cats-effect
[fs2]: https://fs2.io/#/
[fs2 I/O]: https://fs2.io/#/io
[fs2 data csv]: https://fs2-data.gnieh.org/documentation/csv/
[Http4s Ember Client]: https://http4s.org/v0.23/docs/client.html
[Circe]: https://circe.github.io/circe/
[Decline Effect]: https://ben.kirw.in/decline/effect.html
[Munit Cats Effect]: https://github.com/typelevel/munit-cats-effect

[this Enumeration method]: https://github.com/scala/scala/blob/v2.13.8/src/library/scala/Enumeration.scala#L190-L215=
[this comment]: https://github.com/typelevel/cats-effect/issues/3051#issuecomment-1167026949
