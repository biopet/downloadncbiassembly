/*
 * Copyright (c) 2014 Sequencing Analysis Support Core - Leiden University Medical Center
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

import java.io.PrintWriter

import nl.biopet.utils.tool.ToolCommand

import scala.io.Source

object DownloadNcbiAssembly extends ToolCommand[Args] {
  def emptyArgs: Args = Args()
  def argsParser = new ArgsParser(this)
  def main(args: Array[String]): Unit = {
    val cmdArgs = cmdArrayToArgs(args)

    logger.info("Start")

    downloadNcbiassembly(cmdArgs)

    logger.info("Done")
  }

  def downloadNcbiassembly(cmdArgs: Args): Unit = {
    logger.info(s"Reading ${cmdArgs.assemblyReport}")
    val reader = Source.fromFile(cmdArgs.assemblyReport)
    val assamblyReport = reader.getLines().toList
    reader.close()
    cmdArgs.reportFile.foreach { file =>
      val writer = new PrintWriter(file)
      assamblyReport.foreach(writer.println)
      writer.close()
    }

    val headers = assamblyReport
      .filter(_.startsWith("#"))
      .last
      .stripPrefix("# ")
      .split("\t")
      .zipWithIndex
      .toMap
    val nameId = cmdArgs.contigNameHeader.map(x => headers(x))
    val lengthId = headers.get("Sequence-Length")

    val baseUrlEutils = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils"

    val fastaWriter = new PrintWriter(cmdArgs.outputFile)

    val allContigs = assamblyReport
      .filter(!_.startsWith("#"))
      .map(_.split("\t"))
    val totalLength =
      lengthId.map(id => allContigs.map(_.apply(id).toLong).sum)

    logger.info(s"${allContigs.size} contigs found")
    totalLength.foreach(l => logger.info(s"Total length: $l"))

    val filterContigs = allContigs
      .filter(values =>
        cmdArgs.mustNotHave.forall(x => values(headers(x._1)) != x._2))
      .filter(values =>
        cmdArgs.mustHaveOne
          .exists(x => values(headers(x._1)) == x._2) || cmdArgs.mustHaveOne.isEmpty)
    val filterLength =
      lengthId.map(id => filterContigs.map(_.apply(id).toLong).sum)

    logger.info(s"${filterContigs.size} contigs left after filtering")
    filterLength.foreach(l => logger.info(s"Filtered length: $l"))

    filterContigs.foreach { values =>
      val id = if (values(6) == "na") values(4) else values(6)
      logger.info(s"Start download $id")
      val fastaReader =
        Source.fromURL(
          s"$baseUrlEutils/efetch.fcgi?db=nuccore&id=$id&retmode=text&rettype=fasta")
      fastaReader
        .getLines()
        .map(x =>
          nameId.map(y => x.replace(">", s">${values(y)} ")).getOrElse(x))
        .foreach(fastaWriter.println)
      fastaReader.close()
    }

    logger.info("Downloading complete")

    fastaWriter.close()
  }

  def descriptionText: String =
    """
      |This tool downloads an assembly FASTA file from NCBI given an assembly report file. Columns
      |can be filtered for regexes to exist or not exist. Contig name style can be selected.
    """.stripMargin

  def manualText: String =
    s"""
       |$toolName requires an assembly report to download an assembly sequence. It will output
       |the assembly in FASTA format. For filtering, check the usage for more details.
     """.stripMargin

  def exampleText: String =
    s"""
       |For downloading an assembly using the information from an assembly report:
       |${example("-a", "assemblyReport", "-o", "outputFile")}
       |
       |For downloading an assembly and naming the contigs UCSC style:
       |${example("-a",
                  "assemblyReport",
                  "-o",
                  "outputFile",
                  "--nameHeader",
                  "UCSC-style-name")}
     """.stripMargin
}
