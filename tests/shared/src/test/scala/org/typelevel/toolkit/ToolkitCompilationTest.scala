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
import buildinfo.BuildInfo.scalaVersion

class ToolkitCompilationTest extends CatsEffectSuite {

  testCompilation213("Toolkit should compile a simple Hello Cats Effect") {
    """|import cats.effect._
       |
       |object Hello extends IOApp.Simple {
       |  def run = IO.println("Hello toolkit!")
       |}"""
  }

  testRun213("Toolkit should run a simple Hello Cats Effect") {
    """|import cats.effect._
       |
       |object Hello extends IOApp.Simple {
       |  def run = IO.println("Hello toolkit!")
       |}"""
  }

  testCompilation3("Toolkit should compile a simple Hello Cats Effect") {
    """|import cats.effect.*
       |
       |object Hello extends IOApp.Simple:
       |  def run = IO.println("Hello toolkit!")"""
  }

  testRun3("Toolkit should run a simple Hello Cats Effect") {
    """|import cats.effect.*
       |
       |object Hello extends IOApp.Simple:
       |  def run = IO.println("Hello toolkit!")"""
  }

  testCompilation213("Toolkit should compile a script with every dependency") {
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

  testRun213("Toolkit should run a script with every dependency") {
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

  testCompilation3("Toolkit should compile a script with every dependency") {
    """|import cats.syntax.all.*
       |import cats.effect.*
       |import com.monovore.decline.effect.*
       |import fs2.data.csv.generic.semiauto.*
       |import fs2.io.file.*
       |import org.http4s.ember.client.*
       |
       |object Hello extends IOApp.Simple:
       |  def run = IO.println("Hello toolkit!")"""
  }

  testRun3("Toolkit should run a script with every dependency") {
    """|import cats.syntax.all.*
       |import cats.effect.*
       |import com.monovore.decline.effect.*
       |import fs2.data.csv.generic.semiauto.*
       |import fs2.io.file.*
       |import org.http4s.ember.client.*
       |
       |object Hello extends IOApp.Simple:
       |  def run = IO.println("Hello toolkit!")"""
  }

  def testCompilation213 = testCompilation("2.13.12")

  def testCompilation3 = testCompilation("3.3.1")

  def testRun213 = testCompilation("2.13.12")

  def testRun3 = testCompilation("3.3.1")

  def testCompilation(
      expectedLangVersion: String
  )(testName: TestOptions)(scriptBody: String): Unit = test {
    val t = testName.withName(s"${testName.name} - $expectedLangVersion")
    if (scalaVersion == expectedLangVersion) t else t.ignore
  }(ScalaCliProcess.compile(scriptBody))

  def testRun(
      expectedLangVersion: String
  )(testName: TestOptions)(scriptBody: String): Unit = test {
    val t = testName.withName(s"${testName.name} - $expectedLangVersion")
    if (scalaVersion == expectedLangVersion) t else t.ignore
  }(ScalaCliProcess.run(scriptBody))

}
