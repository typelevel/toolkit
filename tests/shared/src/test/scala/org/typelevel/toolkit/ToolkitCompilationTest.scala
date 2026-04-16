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

import cats.effect.IO
import buildinfo.BuildInfo.scala3
import weaver.*
import scala.concurrent.duration.*

object ToolkitCompilationTest extends SimpleIOSuite {

  // 4 minutes may seem a lot, but consider that the first test for
  // each (scalaVersion, platform) will have to download the compiler
  // (if it's not the default), compile (that for native takes awhile)
  // and then finally run the code.
  private val TestTimeout = 4.minutes

  // This helps improve times on Native, more investigation is needed to understand why.
  override val maxParallelism = 1

  testRun("Toolkit should run a simple Hello Cats Effect") {
    if (scala3)
      """|import cats.effect.*
         |
         |object Hello extends IOApp.Simple:
         |  def run = IO.println("Hello toolkit!")"""
    else
      """|import cats.effect._
         |
         |object Hello extends IOApp.Simple {
         |  def run = IO.println("Hello toolkit!")
         |}"""
  }

  testRun("Toolkit should run a script with every dependency") {
    if (scala3)
      """|import cats.syntax.all.*
         |import cats.effect.*
         |import com.monovore.decline.effect.*
         |import fs2.data.csv.generic.semiauto.*
         |import fs2.io.file.*
         |import org.http4s.ember.client.*
         |
         |object Hello extends IOApp.Simple:
         |  def run = IO.println("Hello toolkit!")"""
    else
      """|import cats.syntax.all._
         |import cats.effect._
         |import com.monovore.decline.effect._
         |import fs2.data.csv.generic.semiauto._
         |import fs2.io.file._
         |import io.circe._
         |import org.http4s.ember.client._
         |
         |object Hello extends IOApp.Simple {
         |  def run = IO.println("Hello toolkit!")
         |}"""
  }

  testTest("Toolkit should execute a simple weaver suite") {
    if (scala3)
      """|import cats.effect.IO
         |import weaver.*
         |
         |object Test extends SimpleIOSuite:
         |  test("test")(IO.pure(success))"""
    else
      """|import cats.effect.IO
         |import weaver._
         |
         |object Test extends SimpleIOSuite {
         |  test("test")(IO.pure(success))
         |}"""
  }

  private def withTimeout(test: IO[Expectations]): IO[Expectations] =
    test.timeoutTo(
      TestTimeout,
      IO.pure(failure(s"Test exceeded timeout of $TestTimeout"))
    )

  def testRun(testName: String)(scriptBody: String): Unit =
    test(testName) {
      withTimeout(ScalaCliProcess.run(scriptBody))
    }

  def testTest(testName: String)(scriptBody: String): Unit =
    test(testName) {
      withTimeout(ScalaCliProcess.test(scriptBody))
    }

}
