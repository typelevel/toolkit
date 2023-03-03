# typelevel-toolkit

A toolkit of great libraries to start building Typelevel apps on JVM, Node.js, and Native!

Our very own flavour of the [Scala Toolkit](https://github.com/VirtusLab/toolkit).

## Quick start

```scala
//> using lib "org.typelevel::toolkit::@VERSION@"

import cats.effect.*

object Hello extends IOApp.Simple:
  def run = IO.println("Hello toolkit!")
```