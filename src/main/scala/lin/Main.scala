package lin

import java.io.File

object Main {

  def main(args: Array[String]): Unit = {

    if(args.length == 7) {
      processInputFiles(args(0), args(1), args(2), args(3), args(4), args(5), args(6).toInt)
    } else {
      println("No arguments provided for classUnderTest, inputDirectoryPath, outPackageName, outTestFile, outStatDir, outStatFile and mindepth and maxdepth.")
    }
  }

  def processInputFiles(classUnderTest: String, inputDirectoryPath: String, outPackageName:String, outTestFile: String, statDir: String, outStatFile: String, depth: Int): Unit = {
    if(new File(inputDirectoryPath).isDirectory)
      processDirectory(classUnderTest, inputDirectoryPath, outPackageName, outTestFile, statDir, outStatFile, depth)
    else
      processSingleFile(classUnderTest, inputDirectoryPath, outPackageName, outTestFile, statDir, outStatFile, depth)
  }

  private def processDirectory(classUnderTest: String, inputDirectoryPath: String, outPackageName:String, outTestFile: String, statDir: String, outStatFile: String, depth: Int): Unit = {
    var counter = 1
    def recursiveListFiles(f: File): Array[File] = {
      val files = f.listFiles
      files ++ files.filter(_.isDirectory).flatMap(recursiveListFiles)
    }

    recursiveListFiles(new File(inputDirectoryPath)).filter(_.getName.endsWith(".json"))
      .foreach(x => {
        processSingleFile(classUnderTest, x.getAbsolutePath, outPackageName, outTestFile.concat(counter.toString), statDir, outStatFile, depth)
        counter = counter + 1
      })
  }

  private def processSingleFile(classUnderTest: String, inFile: String, outPackageName:String, outTestFile: String, statDir: String, outStatFile: String, depth: Int): Unit = {
    //println("Processing: " + inFile)
    val hp = new HistoryProcessor(classUnderTest, inFile, outPackageName, outTestFile, statDir, outStatFile, depth = depth)

    if(hp.isLinear) {
      //println("Skipped - Linear history in: " + inFile)
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

