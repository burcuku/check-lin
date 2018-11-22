package lin

import lin.scheduleGen._
import lin.testGen.{TestWriter, Utils}

/**
  * Reads a history of operation calls and creates a Java class which tests different schedules of these operations
  * The parameters for the created tests are read from test.conf file
  *
  * @param inputFile  the name of the history json file
  * @param outputFile the name of the Java class file to be generated
  * @param statFile   the name of the stats file to be generated
  * @param depth the depth of the hitting-families of schedules to be generated
  */
class HistoryProcessor (classUnderTest: String = TestConfig.inputFile,
                        inputFile: String = TestConfig.inputFile,
                        outPckName: String = TestConfig.generatedPckName,
                        outputFile: String = TestConfig.generatedClassName,
                        statDir: String = TestConfig.statsDirName,
                        statFile: String = TestConfig.statsFileName,
                        scheduleEnumeratorType: String = TestConfig.scheduleEnumerator,
                        depth: Int = TestConfig.depth) {

  private val config = new TestConfig(classUnderTest)
  private val poset = HistoryReader.parseIntoPoset(inputFile)
  private val scheduleEnumerator = scheduleEnumeratorType.toUpperCase match {
    case "DHITTING" => new DHitting(depth, poset)
    case _ => new DHitting(depth, poset)  //  new enumerators might be added
  }

  private val schedules: Seq[Seq[Operation]] = scheduleEnumerator.getSchedules //poset.dHittingSchedules(depth)
  private val distinctSchedules = schedules.distinct

  def generateTestFile(): Unit = {
    //println("Generating test file for " + distinctSchedules.size + " schedules from " + inputFile)
    val allSchedules = distinctSchedules.grouped(1500).toSeq

    if(allSchedules.size > 1) {
      var part = 1
      allSchedules.foreach( partOfList => {
        generateTestFile(partOfList, generatedClassNameP = outputFile.concat("Part").concat(part.toString))
        part = part + 1
      })
    } else {
      generateTestFile(distinctSchedules)
    }
  }

  private def generateTestFile(schedules: Seq[Seq[Operation]], generatedClassNameP: String = outputFile): Unit = {
    val generated = new TestWriter(config, schedules,
      expectedResult = poset.result, // should map the id of the events!
      generatedClassName = generatedClassNameP,
      generatedPackageName = outPckName.concat(".D").concat(depth.toString),
      producedFromHistoryFile = inputFile)
    generated.writeClass()
  }

  def generateStatsFile(): Unit = {
    val stats = new StringBuilder()
    stats.append("History-file: ").append(inputFile).append("\n")
    stats.append("Generated-test-file: ").append(Utils.getTestFileName(TestConfig.generatedPckName, outputFile)).append("\n")
    stats.append("d-hitting-depth: ").append(depth).append("\n")
    stats.append("Num-operations: ").append(poset.numOps).append("\n")
    stats.append("Num-concurrent-ops: ").append(poset.numConcurrentOps).append("\n")
    //stats.append("TimeLine: ").append(poset.timeLine).append("\n")
    stats.append("Num-concurrent-op-pairs: ").append(poset.numConcurrentOpPairs).append("\n")
    //stats.append("Num-schedules: ").append(schedules.size).append("\n")
    stats.append("Num-distinct-schedules: ").append(schedules.distinct.size).append("\n")
    if(TestConfig.printSchedulesInStats) {
      stats.append("Distinct-schedules: ")
      schedules.distinct.foreach(s => stats.append("\n").append(s.map(_.id).foldLeft(new StringBuilder){ (sb, s) => sb append s append " " }.toString))
    }
    stats.append("\n\n")

    val statsStr = stats.toString
    Utils.writeToFile(statDir, statFile, statsStr, true)
    //println(statsStr)
  }

  val isLinear: Boolean = poset.concurrentOpPairs.isEmpty
  lazy val numDistinctSchedules: Int = distinctSchedules.size


}
