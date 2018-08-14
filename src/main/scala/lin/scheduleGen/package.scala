package lin

import lin.scheduleGen.json.Event

package object scheduleGen {
  case class Chain(chainId: Int, operations: List[Operation])
  case class TSEvent(ts: Int, event: Event)
  case class ScheduleIndex(chainId: Int, toSingleOut: List[Int])
}
