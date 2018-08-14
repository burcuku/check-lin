package lin.scheduleGen

import scala.collection.mutable.ListBuffer

case class Schedule(scheduleIndex: ScheduleIndex, singledOut: ListBuffer[Operation], sch: ListBuffer[Operation]) {

  def indexInSchedule(eventId: Int): Int = sch.indexWhere(m => m.id == eventId)

  def addEvent(event: Operation): sch.type = sch += event

  def insertEvent(index: Int, event: Operation): Unit = sch.insert(index, event)

  def singleOutEvent(event: Operation): Unit = {
    require(scheduleIndex.toSingleOut.contains(event.id))
    require(sch.contains(event))
    singledOut += event
  }

  /**
    *
    * @param elem an op
    * @return the index of the op in the schedule
    *         - which is the largest indexed which is before elem
    */
  def largestIndexedBefore(elem: Operation): Int = {
    val e = sch.reverse.find(e => e.isBefore(elem))
    e match {
      case Some(ev) =>
        val indexInSchedule = sch.toList.indexOf(ev)
        indexInSchedule
      case None => -1 // all are concurrent or greater than the event
    }
  }

  /**
    *
    * @param elem an op
    * @return the index of the op in the schedule -
    *         smallest indexed singled out which is concurrent
    */
  def smallestIndexedConcSingledOut(elem: Operation): Int = { // indexWhere finds the index in filtered!
    val op = sch.filter(singledOut.contains).find(e => elem.isConcurrent(e))
    op match {
      case Some(m) =>
        val indexInSchedule = sch.toList.indexOf(m) // there is some concurrent
        indexInSchedule
      case None => -1
    }
  }

  def smallestIndexedConcSingledOutIndex(elem: Operation): Int = {
    val op = sch.filter(singledOut.contains).find(e => elem.isConcurrent(e) &&
      scheduleIndex.toSingleOut.indexOf(elem.id) < scheduleIndex.toSingleOut.indexOf(e.id)) // indexWhere finds the index in filtered!
    op match {
      case Some(m) =>
        val indexInSchedule = sch.toList.indexOf(m)
        indexInSchedule
      case None => -1
    }
  }

  def smallestLarger(elem: Operation): Int = sch.indexWhere(e => elem.isAfter(e))

  def repOk: Boolean = {
    // All events appear in the schedule once
    assert(sch.size == sch.toSet.size)
    // The schedule obeys the hb order of the events
    assert(hbOk)
    // Strong hitting
    for(x <- sch) {
      if(x.callEvent.event.sid == scheduleIndex.chainId || scheduleIndex.toSingleOut.contains(x.id)) {
        val lists = sch.toList.splitAt(sch.toList.indexOf(x))
        assert(lists._2.tail.forall(y => (y.isAfter(x)
          || scheduleIndex.toSingleOut.contains(y.id)
          || singledOut.exists(e => y.isAfter(e))
          || y.callEvent.event.sid == scheduleIndex.chainId
          || sch.filter(e => e.callEvent.event.sid == scheduleIndex.chainId).exists(e => y.isAfter(e))
        )))
      }
    }

    true
  }

  def hbOk: Boolean = {
    sch.toList.forall(e1 => {
      val index = sch.indexOf(e1)
      sch.toList.take(index).forall(e2 => !e1.isBefore(e2))
    })
  }

  // only considers the generated element ordering, not the pivot chain and the tuple of singled out elements
  override def equals(other: Any): Boolean = {
    if (!other.isInstanceOf[Schedule]) false
    val o: Schedule = other.asInstanceOf[Schedule]
    sch.zip(o.sch).forall(pair => pair._1 == pair._2)
  }

  override def toString: String = {
    new StringBuilder().append("Chain: ").append(scheduleIndex.chainId)
      .append(" Tuple: ").append(scheduleIndex.toSingleOut).append(" :").append("\n")
      .append(sch.map(_.id)).toString()
  }
}

object Schedule {
  def findSchedule(tuple: Seq[Int], schedules: Seq[Seq[Operation]]): Option[Seq[Operation]] = {
    schedules.foreach(s => {
      val indices = tuple.map(t => s.indexWhere(x => x.id == t))
      if(indices.sorted == indices) return Some(s)
    }
    )
    None
  }

  def findScheduleDep(dep: Seq[(Int, Int)], schedules: Seq[Seq[Operation]]): Option[Seq[Operation]] = {
    schedules.foreach(s => {
      val indices: Seq[(Int, Int)] = dep.map(d => (s.indexWhere(x => x.id == d._1), s.indexWhere(x => x.id == d._2)))
      if(indices.forall(i => List(i._1, i._2).sorted == List(i._1, i._2))) return Some(s)
    })
    None
  }

  def isFeasible(tuple: Seq[Int], operations: Seq[Operation]): Boolean = {
    val ops = tuple.map(i => operations.find(_.id == i))
    assert(ops.forall(op => op.isDefined))
    isFeasible(ops.map(_.get))
  }

  def isFeasible(tuple: Seq[Operation]): Boolean = {
    val pairs: Seq[(Int, Int)] = (0 to tuple.size-2).flatMap(i => (i until tuple.size).map(j => (i, j)))
    pairs.forall(p => !tuple(p._1).isAfter(tuple(p._2)))
  }

  def hbOk(list: Seq[Operation]): Boolean = {
    list.forall(e1 => {
      val index = list.indexOf(e1)
      list.take(index).forall(e2 => !e1.isBefore(e2))
    })
  }

}

