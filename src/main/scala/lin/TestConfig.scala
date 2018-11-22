package lin

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

class TestConfig(val classUnderTest: String) {
  import collection.JavaConversions._

  val imports: List[String] = classUnderTest match {
    case "java.util.concurrent.ArrayBlockingQueue<Integer>" => List("java.util.concurrent.ArrayBlockingQueue")
    case "java.util.concurrent.ConcurrentLinkedDeque<Integer>" => List("java.util.concurrent.ConcurrentLinkedDeque")
    case "java.util.concurrent.ConcurrentLinkedQueue<Integer>" => List("java.util.concurrent.ConcurrentLinkedQueue")
    case "java.util.concurrent.ConcurrentHashMap<Integer,Integer>" => List("java.util.concurrent.ConcurrentHashMap")
    case "java.util.concurrent.ConcurrentSkipListMap<Integer,Integer>" => List("java.util.concurrent.ConcurrentSkipListMap")
    case "java.util.concurrent.ConcurrentSkipListSet<Integer>" => List("java.util.concurrent.ConcurrentSkipListSet")
    case "java.util.concurrent.LinkedBlockingDeque<Integer>" => List("java.util.concurrent.LinkedBlockingDeque")
    case "java.util.concurrent.LinkedBlockingQueue<Integer>" => List("java.util.concurrent.LinkedBlockingQueue")
    case "java.util.concurrent.LinkedTransferQueue<Integer>" => List("java.util.concurrent.LinkedTransferQueue")
    case "java.util.concurrent.PriorityBlockingQueue<Integer>" => List("java.util.concurrent.PriorityBlockingQueue")
  }

  val collParamType: String =  classUnderTest match {
    case "java.util.concurrent.ArrayBlockingQueue<Integer>" => "java.util.concurrent.ArrayBlockingQueue<Integer>"
    case "java.util.concurrent.ConcurrentLinkedDeque<Integer>" => "java.util.concurrent.ConcurrentLinkedDeque<Integer>"
    case "java.util.concurrent.ConcurrentLinkedQueue<Integer>" => "java.util.concurrent.ConcurrentLinkedQueue<Integer>"
    case "java.util.concurrent.ConcurrentHashMap<Integer,Integer>" => "java.util.concurrent.ConcurrentHashMap<Integer,Integer>"
    case "java.util.concurrent.ConcurrentSkipListMap<Integer,Integer>" => "java.util.concurrent.ConcurrentSkipListMap<Integer,Integer>"
    case "java.util.concurrent.ConcurrentSkipListSet<Integer>" => "java.util.concurrent.ConcurrentSkipListSet<Integer>"
    case "java.util.concurrent.LinkedBlockingDeque<Integer>" => "java.util.concurrent.LinkedBlockingDeque<Integer>"
    case "java.util.concurrent.LinkedBlockingQueue<Integer>" => "java.util.concurrent.LinkedBlockingQueue<Integer>"
    case "java.util.concurrent.LinkedTransferQueue<Integer>" => "java.util.concurrent.LinkedTransferQueue<Integer>"
    case "java.util.concurrent.PriorityBlockingQueue<Integer>" => "java.util.concurrent.PriorityBlockingQueue<Integer>"
  }

  val collParamMethod: String = classUnderTest match {
    case "java.util.concurrent.ConcurrentHashMap<Integer,Integer>" => "put"
    case "java.util.concurrent.ConcurrentSkipListMap<Integer,Integer>" => "put"
    case _ => "add"
  }

}

object TestConfig {
  private val config: Config = ConfigFactory.parseFile(new File("test.conf"))

  val inputFile: String = if(config.hasPath("inputFile")) config.getString("inputFile") else "history.json"

  val depth: Int = if(config.hasPath("depth")) config.getInt("depth") else 2
  //val classUnderTest: String = if(config.hasPath("classUnderTest")) config.getString("classUnderTest") else "java.util.concurrent.ConcurrentLinkedQueue<Integer>"

  val generatedPckName: String = if(config.hasPath("generatedPckName")) config.getString("generatedPckName") else "produced.test"
  val generatedClassName: String = if(config.hasPath("generatedClassName")) config.getString("generatedClassName") else "Test"

  //val imports: List[String] = if(config.hasPath("imports")) config.getStringList("imports").toList else List("java.util.concurrent.ConcurrentLinkedQueue")
  val varName: String = if(config.hasPath("testedVarName")) config.getString("testedVarName") else "mObject"
  val varType: String = if(config.hasPath("testedVarType")) config.getString("testedVarType") else "java.util.ArrayList<Integer>"
  val testedMethodPrefix: String = if(config.hasPath("testedMethodPrefix")) config.getString("testedMethodPrefix") else "testSchedule"

  val collParamName: String = if(config.hasPath("collParamName")) config.getString("collParamName") else "mParam"
  //val collParamType: String = if(config.hasPath("collParamType")) config.getString("collParamType") else "java.util.ArrayList<Integer>"
  //val collParamMethod: String = if(config.hasPath("collParamMethod")) config.getString("collParamMethod") else "add"

  val statsDirName: String = if(config.hasPath("statsDirName")) config.getString("statsDirName") else "stats"
  val statsFileName: String = if(config.hasPath("statsFileName")) config.getString("statsFileName") else "Stat"
  val printSchedulesInStats: Boolean = if(config.hasPath("printSchedulesInStats")) config.getBoolean("printSchedulesInStats") else false
  val printSchedulesInTests: Boolean = if(config.hasPath("printSchedulesInTests")) config.getBoolean("printSchedulesInTests") else false

  val scheduleEnumerator: String = if(config.hasPath("scheduleEnumerator")) config.getString("scheduleEnumerator") else "DHitting"
}
