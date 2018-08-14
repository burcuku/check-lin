package lin

import java.io.File

object Main {

  def main(args: Array[String]): Unit = {

    if(args.length == 4) {
      processInputFiles(args(0), args(1), args(2), args(3).toInt)
    } else {
      println("No arguments provided for inputDirectoryPath, outTestFile, outStatFile and depth.")
      println("Running with parameters specified in test.conf.")
      processInputFiles(TestConfig.inputFile, TestConfig.generatedClassName, TestConfig.statsFileName, TestConfig.depth)
    }
  }

  def processInputFiles(inputDirectoryPath: String, outTestFile: String, outStatFile: String, depth: Int): Unit = {
    if(new File(inputDirectoryPath).isDirectory)
      processDirectory(inputDirectoryPath, outTestFile, outStatFile, depth)
    else
      processSingleFile(inputDirectoryPath, outTestFile, outStatFile, depth)
  }

  private def processDirectory(inputDirectoryPath: String, outTestFile: String, outStatFile: String, depth: Int): Unit = {
    var counter = 1
    def recursiveListFiles(f: File): Array[File] = {
      val files = f.listFiles
      files ++ files.filter(_.isDirectory).flatMap(recursiveListFiles)
    }

    recursiveListFiles(new File(inputDirectoryPath)).filter(_.getName.endsWith(".json"))
      .foreach(x => {
        processSingleFile(x.getAbsolutePath, outTestFile.concat(counter.toString), outStatFile, depth)
        counter = counter + 1
      })
  }

  private def processSingleFile(inFile: String, outTestFile: String, outStatFile: String, depth: Int): Unit = {
    //println("Processing: " + inFile)
    val hp = new HistoryProcessor(inFile, outTestFile, outStatFile, depth = depth)

    if(hp.isLinear) {
      println("Skipped - Linear history in: " + inFile)
      return
    }

    try {
      hp.generateStatsFile()
      hp.generateTestFile()
    } catch  {
      case e: OutOfMemoryError => println("Cannot complete test class for: " + inFile + " depth: " + depth)
        System.exit(-1)
      case e: Throwable => println(e.getMessage);
    }
  }
}

