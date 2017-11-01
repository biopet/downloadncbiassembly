package nl.biopet.tools.downloadncbiassembly

import java.io.PrintWriter

import nl.biopet.utils.tool.ToolCommand

import scala.io.Source

object DownloadNcbiAssembly extends ToolCommand[Args] {
  def emptyArgs: Args = Args()
  def argsParser = new ArgsParser(toolName)
  def main(args: Array[String]): Unit = {
    val parser = new ArgsParser(toolName)
    val cmdArgs =
      parser.parse(args, Args()).getOrElse(throw new IllegalArgumentException)

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
    val totalLength = lengthId.map(id => allContigs.map(_.apply(id).toLong).sum)

    logger.info(s"${allContigs.size} contigs found")
    totalLength.foreach(l => logger.info(s"Total length: $l"))

    val filterContigs = allContigs
      .filter(values => cmdArgs.mustNotHave.forall(x => values(headers(x._1)) != x._2))
      .filter(values =>
        cmdArgs.mustHaveOne
          .exists(x => values(headers(x._1)) == x._2) || cmdArgs.mustHaveOne.isEmpty)
    val filterLength = lengthId.map(id => filterContigs.map(_.apply(id).toLong).sum)

    logger.info(s"${filterContigs.size} contigs left after filtering")
    filterLength.foreach(l => logger.info(s"Filtered length: $l"))

    filterContigs.foreach { values =>
      val id = if (values(6) == "na") values(4) else values(6)
      logger.info(s"Start download $id")
      val fastaReader =
        Source.fromURL(s"$baseUrlEutils/efetch.fcgi?db=nuccore&id=$id&retmode=text&rettype=fasta")
      fastaReader
        .getLines()
        .map(x => nameId.map(y => x.replace(">", s">${values(y)} ")).getOrElse(x))
        .foreach(fastaWriter.println)
      fastaReader.close()
    }

    logger.info("Downloading complete")

    fastaWriter.close()
  }
}
