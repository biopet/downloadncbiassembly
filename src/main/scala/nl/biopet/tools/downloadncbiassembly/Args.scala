package nl.biopet.tools.downloadncbiassembly

import java.io.File

case class Args(assemblyReport: File = null,
                outputFile: File = null,
                reportFile: Option[File] = None,
                contigNameHeader: Option[String] = None,
                mustHaveOne: List[(String, String)] = List(),
                mustNotHave: List[(String, String)] = List())
