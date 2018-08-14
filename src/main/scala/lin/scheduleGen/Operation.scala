package lin.scheduleGen

import scala.collection.mutable.ListBuffer

case class Operation(id: Int, callEvent: TSEvent, returnEvent: TSEvent) {

  def isBefore(other: Operation): Boolean =
    this.returnEvent.ts < other.callEvent.ts

  def isAfter(other: Operation): Boolean =
    other.returnEvent.ts < this.callEvent.ts

  def isConcurrent(other: Operation): Boolean =
    !(this.returnEvent.ts < other.callEvent.ts) && !(other.returnEvent.ts < this.callEvent.ts)

  def isBefore(others: List[Operation]): Boolean = {
    others.forall(e => this.isBefore(e))
  }

  def isAfter(others: List[Operation]): Boolean = {
    others.forall(e => e.isBefore(this))
  }

}

object Operation {

  // sorts the operations so that an element at index i as maximal of the smaller indices
  def sort(operations: Seq[Operation]): Seq[Operation] = {
    val list = new ListBuffer[Operation]

    def indexWhereMMaximal(m: Operation): Int = {
      val index = list.indexWhere(e => e.isAfter(m)) // -1 if m is maximal (or the list is empty)
      if(index >= 0) index else list.size
    }
    operations.foreach(m => list.insert(indexWhereMMaximal(m), m))
    assert(Schedule.hbOk(list))
    list
  }
}
