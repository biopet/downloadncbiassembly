package nl.biopet.tools.downloadncbiassembly

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

class DownloadNcbiAssemblyTest extends BiopetTest {
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      DownloadNcbiAssembly.main(Array())
    }
  }
}
