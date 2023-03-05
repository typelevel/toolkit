# Typelevel Toolkit

A toolkit of great libraries to get started building Typelevel apps on JVM, Node.js, and Native! Our very own flavour of the [Scala Toolkit].

```scala
//> using lib "org.typelevel::toolkit::0.0.2"

import cats.effect.*

object Hello extends IOApp.Simple:
  def run = IO.println("Hello toolkit!")
```

It currently includes:

* [Cats] and [Cats Effect]
* [FS2] and [FS2 I/O]
* [http4s Ember client]
* [Circe] and http4s integration
* [Decline Effect]
* [Munit Cats Effect]
* [fs2-data-csv]

[Scala Toolkit]: https://virtuslab.com/blog/scala-toolkit-makes-scala-powerful-straight-out-of-the-box/
[Cats]: https://typelevel.org/cats
[Cats Effect]: https://typelevel.org/cats-effect
[FS2]: https://fs2.io/#/
[FS2 I/O]: https://fs2.io/#/io
[http4s Ember Client]: https://http4s.org/v0.23/docs/client.html
[Circe]: https://circe.github.io/circe/
[Decline Effect]: https://ben.kirw.in/decline/effect.html
[Munit Cats Effect]: https://github.com/typelevel/munit-cats-effect
[fs2-data-csv]: https://fs2-data.gnieh.org/documentation/csv/
