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

import munit.{CatsEffectSuite, TestOptions}
import buildinfo.BuildInfo.scala3

class ToolkitCompilationTest extends CatsEffectSuite {

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

  testTest("Toolkit should execute a simple munit suite") {
    if (scala3)
      """|import cats.effect.*
         |import munit.*
         |
         |class Test extends CatsEffectSuite:
         |  test("test")(IO.unit)"""
    else
      """|import cats.effect._
         |import munit._
         |
         |class Test extends CatsEffectSuite {
         |  test("test")(IO.unit)
         |}"""
  }

  def testRun(testName: TestOptions)(scriptBody: String): Unit =
    test(testName)(ScalaCliProcess.run(scriptBody))

  def testTest(testName: TestOptions)(scriptBody: String): Unit =
    test(testName)(ScalaCliProcess.test(scriptBody))

}
