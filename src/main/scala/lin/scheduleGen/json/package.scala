package lin.scheduleGen

package object json {
  // types read from json history file
  case class History(schema: Schema, events: List[Event], frequency: Frequency)

  case class Schema(id: Int, mClass: String, parameters: List[Parameter], arguments: List[Argument], sequences: List[Sequence], order: List[Int])

  case class Sequence(index: Int, invocations: List[Invocation])

  case class Frequency(count: Int, total: Int)

  //used
  case class Event(invocation: Invocation, kind: String, sid: Int)
  class CallEvent(invocation: Invocation, kind: String, sid: Int) extends Event(invocation: Invocation, kind: String, sid: Int)
  class ReturnEvent(invocation: Invocation, kind: String, sid: Int, val result: String) extends Event(invocation: Invocation, kind: String, sid: Int)

  //used
  case class Invocation(method: Method, resultPosition: Int, arguments: List[Argument], id: Int)

  //used
  case class Method(isVoid: Boolean = false,
                    isReadOnly: Boolean = false,
                    isTrusted: Boolean = false,
                    name: String = null,
                    parameters: List[Parameter] = List(),
                    harnessParameters: HarnessParameters = null)

  case class HarnessParameters(invocations: Int)

  case class Parameter(paramType: String)

  //used
  class Argument
  case class SequenceArgument(arg: Seq[Any]) extends Argument {
    override def toString: String = {
      var str = "( "
      arg.foreach(a => str = str.concat(a.toString + " "))
      str.concat(")")
    }
  }
  case class IntegerArgument(arg: Int) extends Argument {
    override def toString: String = "( " + arg + " )"
  }
  case class TwoIntegersArgument(arg1: Int, arg2: Int) extends Argument {
    override def toString: String = "( " + arg1 + " , " + arg2 + " )"
  }

  case class MapArgument(map: Map[String, Int]) extends Argument {
    override def toString: String = "( " + map.toString + " )"
  }
}
