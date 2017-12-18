package nl.biopet.tools.downloadncbiassembly

import java.io.File

import nl.biopet.utils.tool.{AbstractOptParser, ToolCommand}

class ArgsParser(toolCommand: ToolCommand[Args])
    extends AbstractOptParser[Args](toolCommand) {
  opt[File]('a', "assembly_report") required () unbounded () valueName "<file>" action {
    (x, c) =>
      c.copy(assemblyReport = x)
  } text "refseq ID from NCBI"
  opt[File]('o', "output") required () unbounded () valueName "<file>" action {
    (x, c) =>
      c.copy(outputFile = x)
  } text "output Fasta file"
  opt[File]("report") unbounded () valueName "<file>" action { (x, c) =>
    c.copy(reportFile = Some(x))
  } text "where to write report from ncbi"
  opt[String]("nameHeader") unbounded () valueName "<string>" action {
    (x, c) =>
      c.copy(contigNameHeader = Some(x))
  } text
    """
      | What column to use from the NCBI report for the name of the contigs.
      | All columns in the report can be used but this are the most common field to choose from:
      | - 'Sequence-Name': Name of the contig within the assembly
      | - 'UCSC-style-name': Name of the contig used by UCSC ( like hg19 )
      | - 'RefSeq-Accn': Unique name of the contig at RefSeq (default for NCBI)""".stripMargin
  opt[(String, String)]("mustHaveOne") unbounded () valueName "<column_name=regex>" action {
    (x, c) =>
      c.copy(mustHaveOne = (x._1, x._2) :: c.mustHaveOne)
  } text "This can be used to filter based on the NCBI report, multiple conditions can be given, at least 1 should be true"
  opt[(String, String)]("mustNotHave") unbounded () valueName "<column_name=regex>" action {
    (x, c) =>
      c.copy(mustNotHave = (x._1, x._2) :: c.mustNotHave)
  } text "This can be used to filter based on the NCBI report, multiple conditions can be given, all should be false"
}
