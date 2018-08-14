package lin

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

object TestConfig {
  import collection.JavaConversions._

  private val config: Config = ConfigFactory.parseFile(new File("test.conf"))

  val inputFile: String = if(config.hasPath("inputFile")) config.getString("inputFile") else "history.json"

  val depth: Int = if(config.hasPath("depth")) config.getInt("depth") else 2
  val classUnderTest: String = if(config.hasPath("classUnderTest")) config.getString("classUnderTest") else "java.util.concurrent.ConcurrentLinkedQueue<Integer>"

  val generatedPckName: String = if(config.hasPath("generatedPckName")) config.getString("generatedPckName") else "produced.test"
  val generatedClassName: String = if(config.hasPath("generatedClassName")) config.getString("generatedClassName") else "Test"

  val imports: List[String] = if(config.hasPath("imports")) config.getStringList("imports").toList else List("java.util.concurrent.ConcurrentLinkedQueue")
  val varName: String = if(config.hasPath("testedVarName")) config.getString("testedVarName") else "mObject"
  val varType: String = if(config.hasPath("testedVarType")) config.getString("testedVarType") else "java.util.ArrayList<Integer>"
  val testedMethodPrefix: String = if(config.hasPath("testedMethodPrefix")) config.getString("testedMethodPrefix") else "testSchedule"

  val collParamName: String = if(config.hasPath("collParamName")) config.getString("collParamName") else "mParam"
  val collParamType: String = if(config.hasPath("collParamType")) config.getString("collParamType") else "java.util.ArrayList<Integer>"
  val collParamMethod: String = if(config.hasPath("collParamMethod")) config.getString("collParamMethod") else "add"

  val statsDirName: String = if(config.hasPath("statsDirName")) config.getString("statsDirName") else "stats"
  val statsFileName: String = if(config.hasPath("statsFileName")) config.getString("statsFileName") else "Stat"
  val printSchedulesInStats: Boolean = if(config.hasPath("printSchedulesInStats")) config.getBoolean("printSchedulesInStats") else false
  val printSchedulesInTests: Boolean = if(config.hasPath("printSchedulesInTests")) config.getBoolean("printSchedulesInTests") else false

  val scheduleEnumerator: String = if(config.hasPath("scheduleEnumerator")) config.getString("scheduleEnumerator") else "DHitting"
}
