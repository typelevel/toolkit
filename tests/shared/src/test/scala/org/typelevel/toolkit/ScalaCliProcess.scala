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
import cats.effect.IO
import cats.syntax.parallel.*
import buildinfo.BuildInfo
import fs2.Stream
import fs2.io.file.Files
import fs2.io.process.ProcessBuilder
import weaver.{Expectations, SourceLocation}
import weaver.Expectations.Helpers.{failure, success}

object ScalaCliProcess {

  private val ClassPath: String = BuildInfo.classPath
  private val JavaHome: String = BuildInfo.javaHome

  private def scalaCli(args: List[String])(implicit
      loc: SourceLocation
  ): IO[Expectations] = ProcessBuilder(
    s"$JavaHome/bin/java",
    args.prependedAll(List("-cp", ClassPath, "scala.cli.ScalaCli"))
  ).spawn[IO]
    .use(process =>
      (
        process.exitValue,
        process.stdout.through(fs2.text.utf8.decode).compile.string,
        process.stderr.through(fs2.text.utf8.decode).compile.string
      ).parFlatMapN {
        case (0, _, _)                  => IO.pure(success)
        case (exitCode, stdout, stdErr) =>
          val errorMessage: String = List(
            Option(stdout).filter(_.nonEmpty).map(s => s"[STDOUT]: $s"),
            Option(stdErr).filter(_.nonEmpty).map(s => s"[STDERR]: $s")
          ).foldLeft(
            s"Non zero exit code ($exitCode) for ${args.mkString(" ")}"
          ) {
            case (summary, Some(err)) => s"$err\n$summary"
            case (summary, None)      => summary
          }

          IO.pure(failure(errorMessage))
      }
    )

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
          s"//> using scala ${BuildInfo.scalaBinaryVersion}",
          s"//> using toolkit typelevel:${BuildInfo.version}",
          s"//> using platform ${BuildInfo.platform}"
        )
        Stream(header.mkString("", "\n", "\n"), scriptBody.stripMargin)
          .through(Files[IO].writeUtf8(path))
          .compile
          .drain
      }
      .map(_.toString)

  def command(args: List[String])(implicit
      loc: SourceLocation
  ): IO[Expectations] = scalaCli(args)

  def run(body: String)(implicit loc: SourceLocation): IO[Expectations] =
    writeToFile(body)(false).use(f => scalaCli("run" :: f :: Nil))

  def test(body: String)(implicit loc: SourceLocation): IO[Expectations] =
    writeToFile(body)(true).use(f => scalaCli("test" :: f :: Nil))
}
