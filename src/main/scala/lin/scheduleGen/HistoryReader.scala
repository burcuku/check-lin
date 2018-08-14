package lin.scheduleGen

import lin.scheduleGen.json.HistoryJsonSupport._
import lin.scheduleGen.json.ReturnEvent
import spray.json._

import scala.io.Source

object HistoryReader {

  /**
    * Create the poset of operations given a history json file
    * @param historyFile Json file keeping the calls/results
    * @return A poset of operations
    */
  def parseIntoPoset(historyFile: String): HistoryPoset = {
    val json = Source.fromFile(historyFile).mkString.parseJson
    val history = HistoryJsonFormat.read(json)

    val events = (1 to history.events.size).zip(history.events).map(pair => TSEvent(pair._1, pair._2)).toList

    // matched events: eventid -> call and return
    val operations: Seq[Operation] = events.groupBy(e => e.event.invocation.id)
      .map(pair => Operation(pair._1, pair._2.head, pair._2.last)).toList.sortBy(_.id)

    val results = operations.map(_.returnEvent.event).filter(p => p.isInstanceOf[ReturnEvent]).map(_.asInstanceOf[ReturnEvent].result)

    new HistoryPoset(operations, results)
  }
}



