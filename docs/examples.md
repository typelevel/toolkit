# Examples

This page contains examples of how typelevel-toolkit and [Scala CLI] work together to write single file scripts using all the power of the typelevel libraries.

## POSTing data and writing the response to a file
This example was written by [Koroeskohr] and taken from the [Virtuslab Blog](https://virtuslab.com/blog/scala-toolkit-makes-scala-powerful-straight-out-of-the-box/).

```scala mdoc
//> using lib "org.typelevel::toolkit::@VERSION@"

import cats.effect.*
import io.circe.Decoder
import fs2.Stream
import fs2.io.file.*
import org.http4s.ember.client.*
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.circe.*

object Main extends IOApp.Simple:
  case class Data(value: String)
  given Decoder[Data]           = Decoder.forProduct1("data")(Data.apply)
  given EntityDecoder[IO, Data] = jsonOf[IO, Data]

  def run = EmberClientBuilder.default[IO].build.use { client =>
    val request: Request[IO] =
      Request(Method.POST, uri"https://httpbin.org/anything")
        .withEntity("file.txt bunchofdata")

    client
      .expect[Data](request)
      .map(_.value.split(" "))
      .flatMap { case Array(fileName, content) =>
        IO.println(s"Writing data to $fileName") *>
          Stream(content)
            .through(fs2.text.utf8.encode)
            .through(Files[IO].writeAll(Path(fileName)))
            .compile
            .drain
      }
  }
```

## Command line version of mkString

In this example, [fs2] is used to read a stream of newline delimited strings from standard input and to reconcatenate them with comma by default, while [decline] is leveraged to parse the command line options.

Compiling this example with [scala-native], adding these directives

```
//> using packaging.output "mkString"
//> using platform "native"
//> using nativeMode "release-fast"
```

will produce a native executable that can be used in a similar way as the Scala's standard library `.mkString`:

```sh
$ echo -e "foo\nbar" | ./mkString --prefix "[" -d "," --suffix "]"
// [foo,bar]
```

```scala mdoc:reset:silent
//> using lib "org.typelevel::toolkit::@VERSION@"

import cats.effect.*
import cats.syntax.all.*
import com.monovore.decline.*
import com.monovore.decline.effect.*
import fs2.*
import fs2.io.*

val prefix    = Opts.option[String]("prefix", "").withDefault("")
val delimiter = Opts.option[String]("delimiter", "", "d").withDefault(",")
val suffix    = Opts.option[String]("suffix", "The suffix").withDefault("")

val stringStream: Stream[IO, String] = stdinUtf8[IO](1024 * 1024 * 10)
  .repartition(s => Chunk.array(s.split("\n", -1)))
  .filter(_.nonEmpty)

// inspired by list.mkString
object Main extends CommandIOApp("mkString", "Concatenates strings from stdin"):
  def main = (prefix, delimiter, suffix).mapN { (pre, delim, post) =>
    val stream = Stream(pre) ++ stringStream.intersperse(delim) ++ Stream(post)
    stream.foreach(IO.print).compile.drain.as(ExitCode.Success)
  }
```

[fs2]: https://fs2.io/#/
[decline]: https://ben.kirw.in/decline/
[scala-native]: https://scala-native.org/en/stable/
[Scala CLI]: https://scala-cli.virtuslab.org/
[Koroeskohr]: https://github.com/Koroeskohr