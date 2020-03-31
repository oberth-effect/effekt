package effekt

import java.io.File

import sbt.io._
import sbt.io.syntax._
import org.junit.Test
import org.junit.Assert._

import scala.language.implicitConversions

class RegressionTests {

  val srcFolder = new File("examples")
  val posFiles = (srcFolder / "pos") ** "*.effekt"
  val negFiles = (srcFolder / "neg") ** "*.effekt"

  @Test def positives(): Unit =
    for (file <- posFiles.get) {
      val path = file.getParentFile
      val baseName = file.getName.stripSuffix(".effekt")
      val checkfile = path / (baseName + ".check")

      val out = interpret(file.getPath)

      val expected = if (checkfile.exists()) { IO.read(checkfile).toString } else { "" }

      assertEquals(s"Wrong result: ${file.getPath}\n\n", expected, out)
    }

  @Test def negatives(): Unit =
    for (file <- negFiles.get) {
      val path = file.getParentFile
      val baseName = file.getName.stripSuffix(".effekt")
      val checkfile = path / (baseName + ".check")

      val out = interpret(file.getPath)

      if (checkfile.exists()) {
        val expected = IO.read(checkfile).toString
        assertEquals(s"Wrong result: ${file.getPath}\n\n", expected, out)
      } else {
        ??? // TODO check output for messages.. for now that would be good enough for
        // a neg test
      }
    }

  def interpret(filename: String): String = {
    val compiler = new effekt.Driver {}
    val configs = compiler.createConfig(Seq("--Koutput", "string"))
    configs.verify()
    compiler.compileFile(filename, configs)
    configs.stringEmitter.result().replaceAll("\u001B\\[[;\\d]*m", "")
  }

  /**
   * Generates the check files from the actual outputs.
   *
   * Call from sbt with:
   *    > project effektJVM
   *    > test:console
   *    scala> new effekt.RegressionTests().generateCheckFiles()
   *
   * Check afterwards with:
   *    git diff
   */
  def generateCheckFiles(): Unit = {
    println("Generating check files by running the tests. This can take a while...\n")
    for (file <- (posFiles.get ++ negFiles.get)) {
      val path = file.getParentFile
      val baseName = file.getName.stripSuffix(".effekt")
      val checkfile = path / (baseName + ".check")

      val out = interpret(file.getPath)
      IO.write(checkfile, out)
    }
  }

}
