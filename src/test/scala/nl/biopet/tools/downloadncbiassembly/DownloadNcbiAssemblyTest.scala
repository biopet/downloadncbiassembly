package nl.biopet.tools.downloadncbiassembly


import java.io.File

import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test

import scala.io.Source

class DownloadNcbiAssemblyTest extends ToolTest[Args] {
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
