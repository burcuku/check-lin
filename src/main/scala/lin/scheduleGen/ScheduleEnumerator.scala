package lin.scheduleGen

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

abstract class ScheduleEnumerator(poset: HistoryPoset) {
  def getSchedules: Seq[Seq[Operation]]
}

/**
  * Generates strong d-hitting schedules of the operations in the poset
  * Each schedule indexed by <c,e-tuple>, a singled out event with chain c is strongly-hit
  * @param d Depth of the hitting family
  * @return The d-hitting family of schedules
  */
class DHitting(d: Int, poset: HistoryPoset) extends ScheduleEnumerator(poset) {

  def getSchedules: Seq[Seq[Operation]] = {
    strongDHittingSchedules(d-1, decomposeIntoChains).map(_.sch) // tuple size is one less than d
  }

  private def decomposeIntoChains: Array[Chain] = {
    val chains: Array[Chain] = new Array[Chain](poset.numChains)
    (0 until poset.numChains).foreach(i => chains(i) = Chain(i, List()))

    poset.operations.foreach(mc =>{
      val threadId = mc.callEvent.event.sid
      chains(threadId) = Chain(threadId, mc :: chains(threadId).operations)
    })

    chains.map(c => Chain(c.chainId, c.operations.reverse))
  }

  private def strongDHittingSchedules(tupleSize: Int, chains: Array[Chain]): Seq[Schedule] = {

    val tuples = Utils.generateTuples(tupleSize, poset.operations.map(_.id).toList)
      .filter(t => Schedule.isFeasible(t, poset.operations))
    val scheduleIndices: Seq[ScheduleIndex] = chains.indices.flatMap(chainId => tuples.map(t => ScheduleIndex(chainId,t)))
    val lists: Seq[mutable.ListBuffer[Int]] = scheduleIndices.map(x => mutable.ListBuffer[Int]())
    val schedules = scheduleIndices.zip(lists).map(i => Schedule(i._1, ListBuffer(), ListBuffer()))

    val sortedOperations = Operation.sort(poset.operations)
    sortedOperations.foreach(e =>
      schedules.foreach(s => insertIntoSchedule(s, e))
    )

    //schedules.foreach(s => println("C: " + s.scheduleIndex.chainId + " Index: " + s.scheduleIndex.toSingleOut + "   Schedule: " + s.sch.map(_.id)))

    // assert all schedules are valid
    assert(schedules.forall(_.repOk))

    // assert for all tuples, we have an ordering:
    tuples.foreach(t =>
      assert(!Schedule.isFeasible(t.map(t => poset.findOperationById(t).get)) || Schedule.findSchedule(t, schedules.map(_.sch)).isDefined))

    val s = schedules.distinct
    s
  }

  private def insertIntoSchedule(schedule:Schedule, elem: Operation): Unit = {
    // 1. if the element needs to be singled out
    if(schedule.scheduleIndex.toSingleOut.contains(elem.id)) {
      val indexAtTuple = schedule.scheduleIndex.toSingleOut.indexOf(elem.id)
      lazy val singledOutIndices = schedule.singledOut.map(e => schedule.scheduleIndex.toSingleOut.indexOf(e.id))

      // 1.a. if l = 0 or i > il or x is greater than xil, schedule x as the last element in the schedule
      if(schedule.singledOut.isEmpty || indexAtTuple > singledOutIndices.max || elem.isAfter(schedule.singledOut.toList)) {
        //println("Case 1a")
        schedule.addEvent(elem)
        schedule.singleOutEvent(elem)
      } else {
        //println("Case 1b")
        // index of the (smallest concurrent and smallest higher index than elem) // -1 if none (all greater)
        val index = schedule.smallestIndexedConcSingledOutIndex(elem)
        //// need to check if it is "after" the later indexed ops!
        val largestIndexedBefore = schedule.largestIndexedBefore(elem)+1 // 0 is all are greater
        schedule.insertEvent(Integer.max(index, largestIndexedBefore), elem)
        schedule.singleOutEvent(elem)
      }
      return
    }

    // 2. if the element is not singled out but is in the chain of chainId of the scheduleIndex
    if(schedule.scheduleIndex.chainId == elem.callEvent.event.sid) {
      // 2.a if l = 0 or x is after xil, schedule x as the last element in the schedule
      if(schedule.singledOut.isEmpty || elem.isAfter(schedule.singledOut.toList.last)) {
        //println("Case 2a")
        schedule.addEvent(elem)
      }

      // 2.b schedule x right before the singled out which is smallest incomparable to x
      else {
        //println("Case 2b")
        val smallestConcSingledOut = schedule.smallestIndexedConcSingledOut(elem)
        //// need to check if it is "after" the later indexed ops!
        val largestIndexedBefore = schedule.largestIndexedBefore(elem)+1
        val index = Integer.max(smallestConcSingledOut, largestIndexedBefore)
        schedule.insertEvent(index, elem)
      }
      return
    }

    // 3. if the element is not singled out and not in the chain of chainId of the scheduleIndex
    if(schedule.scheduleIndex.chainId != elem.callEvent.event.sid) {
      //println("Case 3")
      // we schedule x right after the last y such that y is before x
      val index: Int = schedule.largestIndexedBefore(elem)
      if(index == -1) // none is larger, might be concurrent
        schedule.insertEvent(0, elem)
      else
        schedule.insertEvent(index+1, elem)

      return
    }
  }

}