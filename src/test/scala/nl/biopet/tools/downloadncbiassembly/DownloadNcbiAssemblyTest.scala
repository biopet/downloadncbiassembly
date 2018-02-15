/*
 * Copyright (c) 2014 Biopet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.biopet.tools.downloadncbiassembly

import java.io.File

import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test

import scala.io.Source

class DownloadNcbiAssemblyTest extends ToolTest[Args] {
  def toolCommand: DownloadNcbiAssembly.type = DownloadNcbiAssembly
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      DownloadNcbiAssembly.main(Array())
    }
  }

  @Test
  def testNC_003403_1(): Unit = {
    val output = File.createTempFile("test.", ".fasta")
    val outputReport = File.createTempFile("test.", ".report")
    output.deleteOnExit()
    outputReport.deleteOnExit()
    DownloadNcbiAssembly.main(
      Array("-a",
            new File(resourcePath("/GCF_000844745.1.report")).getAbsolutePath,
            "-o",
            output.getAbsolutePath,
            "--report",
            outputReport.getAbsolutePath))

    Source.fromFile(output).getLines().toList shouldBe Source
      .fromFile(new File(resourcePath("/NC_003403.1.fasta")))
      .getLines()
      .toList
    Source.fromFile(outputReport).getLines().toList shouldBe Source
      .fromFile(new File(resourcePath("/GCF_000844745.1.report")))
      .getLines()
      .toList
  }
}
