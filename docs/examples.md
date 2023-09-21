# Examples

This page contains examples of how typelevel-toolkit and [Scala CLI] work together to write single file scripts using all the power of the Typelevel's libraries.

## POSTing data and writing the response to a file
This example was written by [Koroeskohr] and taken from the [Virtuslab Blog](https://virtuslab.com/blog/scala-toolkit-makes-scala-powerful-straight-out-of-the-box/).

@:select(scala-version)

@:choice(scala-3)

```scala mdoc:silent
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
@:choice(scala-2)

```scala mdoc:reset:silent
//> using lib "org.typelevel::toolkit::@VERSION@"

import cats.effect._
import io.circe.Decoder
import fs2.Stream
import fs2.io.file._
import org.http4s.ember.client._
import org.http4s._
import org.http4s.implicits._
import org.http4s.circe._

object Main extends IOApp.Simple {
  case class Data(value: String)
  implicit val json: Decoder[Data]          = Decoder.forProduct1("data")(Data.apply)
  implicit val enc: EntityDecoder[IO, Data] = jsonOf[IO, Data]

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
}
```

@:@

## Command line version of mkString

In this example, [fs2] is used to read a stream of newline delimited strings from standard input and to reconcatenate them with comma by default, while [decline] is leveraged to parse the command line options.

Compiling this example with [scala-native], adding these directives

```scala mdoc:reset:silent
//> using packaging.output "mkString"
//> using platform "native"
//> using nativeMode "release-fast"
```

will produce a native executable that can be used in a similar way as the Scala's standard library `.mkString`:

```sh
$ echo -e "foo\nbar" | ./mkString --prefix "[" -d "," --suffix "]"
// [foo,bar]
```

@:select(scala-version)

@:choice(scala-3)
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

@:choice(scala-2)

```scala mdoc:reset:silent
//> using lib "org.typelevel::toolkit::@VERSION@"

import cats.effect._
import cats.syntax.all._
import com.monovore.decline._
import com.monovore.decline.effect._
import fs2._
import fs2.io._

// inspired by list.mkString
object Main extends CommandIOApp("mkString", "Concatenates strings from stdin") {
  val prefix    = Opts.option[String]("prefix", "").withDefault("")
  val delimiter = Opts.option[String]("delimiter", "", "d").withDefault(",")
  val suffix    = Opts.option[String]("suffix", "The suffix").withDefault("")

  val stringStream: Stream[IO, String] = stdinUtf8[IO](1024 * 1024 * 10)
    .repartition(s => Chunk.array(s.split("\n", -1)))
    .filter(_.nonEmpty)

  def main = (prefix, delimiter, suffix).mapN { (pre, delim, post) =>
    val stream = Stream(pre) ++ stringStream.intersperse(delim) ++ Stream(post)
    stream.foreach(IO.print).compile.drain.as(ExitCode.Success)
  }
}
```

@:@

## Parsing and transforming a CSV file

Here, [fs2-data-csv] is used to read and parse a comma separated file.
Manual encoders and decoders are defined for our `Passenger`s to show you how to do everything from scratch.

Let's start with a CSV file that has records of fictious passengers registered for a flight:

```
id,First Name,Age,flight number,destination
1,Seyton,44,WX122,Tanzania
2,Lina,,UX199,Greenland
3,Grogu,,SW999,Singapore
```


@:select(scala-version)

@:choice(scala-3)
```scala mdoc:reset:silent
//> using lib "org.typelevel::toolkit::@VERSION@"

import cats.effect.*
import fs2.text
import fs2.data.csv.*
import fs2.data.csv.generic.semiauto.*
import fs2.io.file.{Path, Flags, Files}
import cats.data.NonEmptyList

case class Passenger(
    id: Long,
    firstName: String,
    age: Either[String, Int],
    flightNumber: String,
    destination: String
)

object Passenger:
  // Here we define a manual decoder for each row in our CSV
  given csvRowDecoder: CsvRowDecoder[Passenger, String] with
    def apply(row: CsvRow[String]): DecoderResult[Passenger] =
      for
        id <- row.as[Long]("id")
        firstName <- row.as[String]("First Name")
        ageOpt <- row.asOptional[Int]("Age")
        flightNumber <- row.as[String]("flight number")
        destination <- row.as[String]("destination")
      yield
        val age = ageOpt.toRight[String]("N/A")
        Passenger(id, firstName, age, flightNumber, destination)

    // Here we define a manual encoder for encoding Passenger classes to a CSV
    given csvRowEncoder: CsvRowEncoder[Passenger, String] with
      def apply(p: Passenger): CsvRow[String] =
        CsvRow.fromNelHeaders(
          NonEmptyList.of(
            (p.firstName, "first_name"),
            (p.age.toString(), "age"),
            (p.flightNumber, "flight_number"),
            (p.destination, "destination")
          )
        )

val input = Files[IO]
  .readAll(Path("./example.csv"), 1024, Flags.Read)
  .through(text.utf8.decode)
  .through(decodeUsingHeaders[Passenger]())

object CSVPrinter extends IOApp.Simple:

  /** First we'll do some logging for each row,
    * and then calculate and print the mean age */
  val run =
    input
      .evalTap(p =>
        IO.println(
          s"${p.firstName} is taking flight: ${p.flightNumber} to ${p.destination}"
        )
      )
      .collect({ case Passenger(_, _, Right(age), _, _) => age })
      .foldMap(age => (age, 1))
      .compile
      .lastOrError
      .flatMap((sum, count) =>
        IO.println(s"The mean age of the passengers is ${sum / count}")
      )
```


@:choice(scala-2)
```scala mdoc:reset:silent
//> using lib "org.typelevel::toolkit::@VERSION@"

import cats.effect._
import fs2.text
import fs2.data.csv._
import fs2.data.csv.generic.semiauto._
import fs2.io.file.{Path, Flags, Files}
import cats.data.NonEmptyList

case class Passenger(
    id: Long,
    firstName: String,
    age: Either[String, Int],
    flightNumber: String,
    destination: String
)

object Passenger {
  // Here we define a manual decoder for each row in our CSV
  implicit val csvRowDecoder: CsvRowDecoder[Passenger, String] =
    new CsvRowDecoder[Passenger, String] {
      def apply(row: CsvRow[String]): DecoderResult[Passenger] =
        for {
          id <- row.as[Long]("id")
          firstName <- row.as[String]("First Name")
          ageOpt <- row.asOptional[Int]("Age")
          flightNumber <- row.as[String]("flight number")
          destination <- row.as[String]("destination")
        } yield {
          val age = ageOpt.toRight[String]("N/A")
          Passenger(id, firstName, age, flightNumber, destination)
        }
    }

  // Here we define a manual encoder for encoding Passenger classes to a CSV
  implicit val csvRowEncoder: CsvRowEncoder[Passenger, String] =
    new CsvRowEncoder[Passenger, String] {
      def apply(p: Passenger): CsvRow[String] =
        CsvRow.fromNelHeaders(
          NonEmptyList.of(
            (p.firstName, "first_name"),
            (p.age.toString(), "age"),
            (p.flightNumber, "flight_number"),
            (p.destination, "destination")
          )
        )
    }
}

object CSVPrinter extends IOApp.Simple {
  val input = Files[IO]
    .readAll(Path("./example.csv"), 1024, Flags.Read)
    .through(text.utf8.decode)
    .through(decodeUsingHeaders[Passenger]())


  /** First we'll do some logging for each row,
    * and then calculate and print the mean age */
  val run =
    input
      .evalTap(p =>
        IO.println(
          s"${p.firstName} is taking flight: ${p.flightNumber} to ${p.destination}"
        )
      )
      .collect({ case Passenger(_, _, Right(age), _, _) => age })
      .foldMap(age => (age, 1))
      .compile
      .lastOrError
      .flatMap({ case (sum, count) =>
        IO.println(s"The mean age of the passengers is ${sum / count}")
      })
}
```
@:@


## Parsing and transforming raw data
This real world example was written by [Thanh Le] to convert a file for the [scalachess library](https://github.com/lichess-org/scalachess). The file is used for testing the correctness of its legal moves generator.

Start with an input file named `fischer.epd` containing:

```
bqnb1rkr/pp3ppp/3ppn2/2p5/5P2/P2P4/NPP1P1PP/BQ1BNRKR w HFhf - 2 9 ;D1 21 ;D2 528 ;D3 12189 ;D4 326672 ;D5 8146062 ;D6 227689589
```

The result will be a `chess960.perft` containing:

```
id 0
epd bqnb1rkr/pp3ppp/3ppn2/2p5/5P2/P2P4/NPP1P1PP/BQ1BNRKR w HFhf - 2 9
perft 1 21
perft 2 528
perft 3 12189
perft 4 326672
perft 5 8146062
perft 6 227689589
```

@:select(scala-version)

@:choice(scala-3)

```scala mdoc:reset:silent
//> using lib "org.typelevel::toolkit::@VERSION@"

import cats.effect.{IO, IOApp}
import fs2.{Stream, text}
import fs2.io.file.{Files, Path}

object PerftConverter extends IOApp.Simple:

  val converter: Stream[IO, Unit] =
    def raw2Perft(id: Long, raw: String): String =
      val list = raw.split(";").zipWithIndex.map {
        case (epd, 0) => s"epd ${epd}"
        case (s, i)   => s"perft $i ${s.split(" ")(1)}"
      }
      list.mkString(s"id $id\n", "\n", "\n")

    Files[IO]
      .readUtf8Lines(Path("fischer.epd"))
      .filter(s => !s.trim.isEmpty)
      .zipWithIndex
      .map((x, i) => raw2Perft(i, x))
      .intersperse("\n")
      .through(text.utf8.encode)
      .through(Files[IO].writeAll(Path("chess960.perft")))

  def run: IO[Unit] = converter.compile.drain
```

@:choice(scala-2)
```scala mdoc:reset:silent
//> using lib "org.typelevel::toolkit::@VERSION@"

import cats.effect.{IO, IOApp}
import fs2.{Stream, text}
import fs2.io.file.{Files, Path}

object PerftConverter extends IOApp.Simple {

  val converter: Stream[IO, Unit] = {
    def raw2Perft(id: Long, raw: String): String = {
      val list = raw.split(";").zipWithIndex.map {
        case (epd, 0) => s"epd ${epd}"
        case (s, i)   => s"perft $i ${s.split(" ")(1)}"
      }
      list.mkString(s"id $id\n", "\n", "\n")
    }

    Files[IO]
      .readUtf8Lines(Path("fischer.epd"))
      .filter(s => !s.trim.isEmpty)
      .zipWithIndex
      .map { case (x, i) => raw2Perft(i, x) }
      .intersperse("\n")
      .through(text.utf8.encode)
      .through(Files[IO].writeAll(Path("chess960.perft")))
  }

  def run: IO[Unit] = converter.compile.drain
}
```
@:@

## Writing data to a CSV file

If you want to save a list of a case class into a CSV file this helper method may aid you:

@:select(scala-version)

@:choice(scala-3)
```scala mdoc:reset:silent
import fs2.io.file.Files
import fs2.data.csv.*
import fs2.data.csv.generic.semiauto.*
import fs2.io.file.Path
import cats.effect.IO
import fs2.Pipe

case class YourCaseClass(n: String, i: Int)

given CsvRowEncoder[YourCaseClass, String] = deriveCsvRowEncoder

def writeCaseClassToCsv[A](path: Path)(using CsvRowEncoder[A, String]): Pipe[IO, A, Nothing] =
    _.through(encodeUsingFirstHeaders(fullRows = true))
      .through(fs2.text.utf8.encode)
      .through(Files[IO].writeAll(path))
      .drain

// Usage
fs2.Stream.emits(Seq(YourCaseClass("s", 1))).through(writeCaseClassToCsv(Path("temp.csv")))

```

@:choice(scala-2)

```scala mdoc:reset:silent
import fs2.io.file.Files
import fs2.data.csv._
import fs2.data.csv.generic.semiauto._
import fs2.io.file.Path
import cats.effect.IO
import fs2.Pipe

case class YourCaseClass(n: String, i: Int)

implicit val csvRowEncoder: CsvRowEncoder[YourCaseClass, String] = deriveCsvRowEncoder

def writeCaseClassToCsv[A](path: Path)(using CsvRowEncoder[A, String]): Pipe[IO, A, Nothing] =
    _.through(encodeUsingFirstHeaders(fullRows = true))
      .through(fs2.text.utf8.encode)
      .through(Files[IO].writeAll(path))
      .drain

// Usage
fs2.Stream.emits(Seq(YourCaseClass("s", 1))).through(writeCaseClassToCsv(Path("temp.csv")))

```

@:@



[fs2]: https://fs2.io/#/
[fs2-data-csv]: https://fs2-data.gnieh.org/documentation/csv/
[decline]: https://ben.kirw.in/decline/
[scala-native]: https://scala-native.org/en/stable/
[Scala CLI]: https://scala-cli.virtuslab.org/
[Koroeskohr]: https://github.com/Koroeskohr
[Thanh Le]: https://github.com/lenguyenthanh
