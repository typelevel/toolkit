/*
 * Copyright 2023 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.toolkit

import cats.effect.kernel.Resource
import cats.effect.std.Console
import cats.effect.IO
import buildinfo.BuildInfo
import fs2.Stream
import fs2.io.file.Files
import fs2.io.process.{Process, ProcessBuilder}
import munit.Assertions.fail

object ScalaCliProcess {

  private val ClassPath: String = BuildInfo.classPath
  private val JavaHome: String = BuildInfo.javaHome

  private def scalaCli(args: List[String]): IO[Unit] = ProcessBuilder(
    s"$JavaHome/bin/java",
    args.prependedAll(List("-cp", ClassPath, "scala.cli.ScalaCli"))
  ).spawn[IO]
    .use(process =>
      process.exitValue.flatMap {
        case 0 => IO.unit
        case x =>
          printStreams(process) >> IO.delay(
            fail(s"Non zero exit code ($x) for ${args.mkString(" ")}")
          )
      }
    )

  private def printStreams(process: Process[IO]): IO[Unit] = {
    val stdout: IO[Unit] = process.stdout
      .through(fs2.text.utf8.decode)
      .foreach(Console[IO].print)
      .compile
      .drain
    val stderr: IO[Unit] = process.stderr
      .through(fs2.text.utf8.decode)
      .foreach(Console[IO].error)
      .compile
      .drain
    stdout.both(stderr).void
  }

  private def writeToFile(
      scriptBody: String
  )(isTest: Boolean): Resource[IO, String] =
    Files[IO]
      .tempFile(
        None,
        "",
        if (isTest) "-toolkit.test.scala" else "-toolkit.scala",
        None
      )
      .evalTap { path =>
        val header = List(
          s"//> using scala ${BuildInfo.scalaVersion}",
          s"//> using toolkit typelevel:${BuildInfo.version}",
          s"//> using platform ${BuildInfo.platform}"
        ).mkString("", "\n", "\n")
        Stream(header, scriptBody.stripMargin)
          .through(Files[IO].writeUtf8(path))
          .compile
          .drain
      }
      .map(_.toString)

  def command(args: List[String]): IO[Unit] = scalaCli(args)

  def run(body: String): IO[Unit] =
    writeToFile(body)(false).use(f => scalaCli("run" :: f :: Nil))

  def test(body: String): IO[Unit] =
    writeToFile(body)(true).use(f => scalaCli("test" :: f :: Nil))

}
