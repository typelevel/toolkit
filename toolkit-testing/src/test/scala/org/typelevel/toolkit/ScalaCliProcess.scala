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

import fs2.io.process.{Process, ProcessBuilder, Processes}
import cats.effect.kernel.Concurrent
import cats.effect.std.Console
import cats.syntax.all._
import cats.effect.syntax.all._
import cats.effect.std.Supervisor
import cats.ApplicativeThrow

object ScalaCliProcess {

  private val ClassPath: String =
    System.getProperty("toolkit.testing.classpath")
  private val JavaHome: String = {
    val path = sys.env.get("JAVA_HOME").orElse(sys.props.get("java.home")).get
    if (path.endsWith("/jre")) {
      // handle JDK 8 installations
      path.replace("/jre", "")
    } else path
  }

  private def scalaCli[F[_]: Processes: ApplicativeThrow: Concurrent: Console](
      args: List[String]
  ): F[Unit] = ProcessBuilder(
    s"$JavaHome/bin/java",
    args.prependedAll(List("-cp", ClassPath, "scala.cli.ScalaCli"))
  ).spawn[F]
    .use(process =>
      process.exitValue.flatMap {
        case 0 => ApplicativeThrow[F].unit
        case x =>
          printStreams(process) >> ApplicativeThrow[F].raiseError(
            new Exception(s"Non zero exit code ($x) for ${args.mkString(" ")}")
          )
      }
    )

  private def printStreams[F[_]: Concurrent: Console](
      process: Process[F]
  ): F[Unit] =
    Supervisor[F](await = true).use(supervisor =>
      for {
        _ <- process.stdout
          .through(fs2.text.utf8.decode)
          .foreach(Console[F].println)
          .compile
          .drain
          .supervise(supervisor)
        _ <- process.stderr
          .through(fs2.text.utf8.decode)
          .foreach(Console[F].errorln)
          .compile
          .drain
          .supervise(supervisor)
      } yield ()
    )

  def command[F[_]: Processes: ApplicativeThrow: Concurrent: Console](
      args: List[String]
  ): F[Unit] = scalaCli[F](args)

  def compile[F[_]: Processes: ApplicativeThrow: Concurrent: Console](
      fileName: String
  ): F[Unit] = scalaCli[F]("compile" :: fileName :: Nil)

  def run[F[_]: Processes: ApplicativeThrow: Concurrent: Console](
      fileName: String
  ): F[Unit] = scalaCli[F]("run" :: fileName :: Nil)

}
