package lin.scheduleGen

import scala.collection.mutable.ListBuffer

class HistoryPoset(val operations: Seq[Operation], val result: Seq[String]) {

  /**
    * @return the number of chains in the poset
    * (equal to the number of threads in the linearizability tests)
    */
  lazy val numChains: Int =
    operations.map(_.callEvent.event.sid).distinct.size

  /**
    * @return the number of operations in the poset
    */
  lazy val numOps: Int = operations.size

  /**
    * @return the number of pairs of operations concurrent to each other
    */
  lazy val numConcurrentOpPairs: Int = concurrentOpPairs.length

  lazy val concurrentOpPairs: Seq[(Operation, Operation)] =
  {
    val pairs: ListBuffer[(Operation, Operation)] = new ListBuffer[(Operation, Operation)]
    operations.foreach(e1 => {
      val index = operations.indexOf(e1)
      operations.splitAt(index+1)._2.foreach(e2 => if(e1.isConcurrent(e2) && !pairs.contains((e2, e1))) pairs.insert(0, (e1, e2)))})
    pairs
  }

  lazy val concurrentOpIds: Seq[(Int, Int)] =  concurrentOpPairs.map(x => (x._1.id, x._2.id))
  /**
    * @return the number of pairs of operations concurrent to each other
    */
  lazy val numConcurrentOps: Int = timeLine.max

  lazy val timeLine: Seq[Int] = {
    val timeLine = Array.fill(operations.map(_.returnEvent.ts).max + 1){0}
    operations.foreach(o =>
      (o.callEvent.ts until o.returnEvent.ts).foreach(i => timeLine(i) = timeLine(i) + 1))
    timeLine.toSeq
  }

  def findOperationById(id: Int): Option[Operation] = {
    operations.find(m => m.id == id)
  }
}