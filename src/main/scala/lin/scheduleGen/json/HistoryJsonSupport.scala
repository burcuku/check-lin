package lin.scheduleGen.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

object HistoryJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object SchemaJsonFormat extends RootJsonFormat[Schema] {

    def write(s: Schema): JsObject = JsObject(
      "id" -> JsNumber(s.id),
      "class" -> JsString(s.mClass),
      "parameters" -> JsArray(s.parameters.map(ParameterJsonFormat.write).toVector),
      "arguments" -> JsArray(s.arguments.map(ArgumentJsonFormat.write).toVector),
      "sequences" -> JsArray(s.sequences.map(SequenceJsonFormat.write).toVector),
      "order" -> JsArray(s.order.map(_.toJson).toVector))

    def read(json: JsValue): Schema = {
      val fields = json.asJsObject.fields
      Schema(
        fields("id").convertTo[Int],
        fields("class").convertTo[String],
        fields("parameters").asInstanceOf[JsArray].elements.map(_.asInstanceOf[JsObject]).map(ParameterJsonFormat.read(_)).toList,
        if(fields("arguments").asInstanceOf[JsArray].elements.nonEmpty && fields("arguments").asInstanceOf[JsArray].elements.head.isInstanceOf[JsObject])
          fields("arguments").asInstanceOf[JsArray].elements.map(_.asInstanceOf[JsObject]).map(ArgumentJsonFormat.read(_)).toList
        else
          fields("arguments").asInstanceOf[JsArray].elements.map(_.asInstanceOf[JsNumber]).map(ArgumentJsonFormat.read(_)).toList,
        fields("sequences").asInstanceOf[JsArray].elements.map(_.asInstanceOf[JsObject]).map(SequenceJsonFormat.read(_)).toList,
        fields("order").asInstanceOf[JsArray].elements.map(_.convertTo[Int]).toList)
    }
  }

  implicit object SequenceJsonFormat extends RootJsonFormat[Sequence] {

    def write(s: Sequence): JsObject = JsObject(
      "index" -> JsNumber(s.index),
      "invocations" -> JsArray(s.invocations.map(InvocationJsonFormat.write).toVector))

    def read(json: JsValue): Sequence = {
      val fields = json.asJsObject.fields
      Sequence(
        fields("index").convertTo[Int],
        fields("invocations").asInstanceOf[JsArray].elements.map(_.asInstanceOf[JsObject]).map(InvocationJsonFormat.read(_)).toList)
    }
  }

  implicit object FrequencyJsonFormat extends RootJsonFormat[Frequency] {
    def write(f: Frequency): JsObject = JsObject(
      "count" -> JsNumber(f.count),
      "total" -> JsNumber(f.total)
    )

    def read(json: JsValue): Frequency = {
      val fields = json.asJsObject.fields
      Frequency(fields("count").convertTo[Int], fields("total").convertTo[Int])
    }
  }

  implicit object ParameterJsonFormat extends RootJsonFormat[Parameter] {

    def write(p: Parameter): JsObject = JsObject(
      "mType" -> JsString(p.paramType)
    )

    def read(json: JsValue): Parameter = {
      //val fields = json.asJsObject.fields
      //Parameter(fields("mType").convertTo[String])
      Parameter("java.lang.Object") //todo
    }
  }

  implicit object ArgumentJsonFormat extends RootJsonFormat[Argument] {

    def write(a: Argument): JsObject = a match {
      case ia: IntegerArgument => JsObject("arg" -> JsNumber(ia.arg))
      case tia: TwoIntegersArgument => JsObject(
        "0" -> JsNumber(tia.arg1),
        "1" -> JsNumber(tia.arg2)
      )
    }

    // single int is directly read as JsNumber
    def read(json: JsValue): Argument = {
      if(json.isInstanceOf[JsObject]) {
        val fields = json.asJsObject.fields
        TwoIntegersArgument(fields("0").convertTo[Int], fields("1").convertTo[Int])
      }
      else IntegerArgument(json.convertTo[Int])
    }
  }

  implicit object HarnessParametersJsonFormat extends RootJsonFormat[HarnessParameters] {

    def write(hp: HarnessParameters): JsObject = JsObject(
      "invocations" -> JsNumber(hp.invocations)
    )

    def read(json: JsValue): HarnessParameters = {
      val fields = json.asJsObject.fields
      HarnessParameters(fields("invocations").convertTo[Int])
    }
  }

  implicit object MethodJsonFormat extends RootJsonFormat[Method] {

    def write(m: Method): JsObject = JsObject(
      "void" -> JsBoolean(m.isVoid),
      "readonly" -> JsBoolean(m.isReadOnly),
      "trusted" -> JsBoolean(m.isTrusted),
      "name" -> JsString(m.name),
      "harnessParameters" -> HarnessParametersJsonFormat.write(m.harnessParameters),
      "parameters" -> JsArray(m.parameters.map(_.toJson).toVector))

    def read(json: JsValue): Method = {
      val fields = json.asJsObject.fields

      Method(fields("void").convertTo[Boolean],
        fields("readonly").convertTo[Boolean],
        if(fields.keySet.contains("trusted")) fields("trusted").convertTo[Boolean] else false,
        fields("name").convertTo[String],
        fields("parameters").asInstanceOf[JsArray].elements.map(ParameterJsonFormat.read).toList,
        if(fields.keySet.contains("harnessParameters")) HarnessParametersJsonFormat.read(fields("harnessParameters")) else null)
    }
  }

  implicit object InvocationJsonFormat extends RootJsonFormat[Invocation] {

    def write(i: Invocation): JsObject = JsObject(
      "method" -> MethodJsonFormat.write(i.method),
      "resultPosition" -> JsNumber(i.resultPosition),
      "arguments" -> JsArray(i.arguments.map(_.toJson).toVector),
      "id" -> JsNumber(i.id))

    def read(json: JsValue): Invocation = {
      val fields = json.asJsObject.fields
      Invocation(MethodJsonFormat.read(fields("method")),
        if(fields.keySet.contains("resultPosition")) fields("resultPosition").convertTo[Int] else 0,
        processArguments(fields("arguments").asInstanceOf[JsArray]),
        fields("id").convertTo[Int])
    }

    def processArguments(jsArray: JsArray): List[Argument] = {
      jsArray.elements.map { x =>
        any2Arg(AnyJsonFormat.read(x))
      }.toList
    }

    def any2Arg(a: Any) = a match {
      case x: Int => IntegerArgument(x)
      case x: Seq[Int] => SequenceArgument(x)
      case x: Map[String, Int] => MapArgument(x)
    }
  }

  implicit object EventJsonFormat extends RootJsonFormat[Event] {

    def write(e: Event): JsObject = JsObject(
      "invocation" -> InvocationJsonFormat.write(e.invocation),
      "kind" -> JsString(e.kind),
      "sid" -> JsNumber(e.sid))

    def read(json: JsValue): Event = {
      val fields = json.asJsObject.fields
      fields("kind").convertTo[String] match {
        case "call" => new CallEvent(InvocationJsonFormat.read(
          fields("invocation")),
          fields("kind").convertTo[String],
          fields("sid").convertTo[Int])
        case "return" if fields.keySet.contains("value") =>
          new ReturnEvent(InvocationJsonFormat.read(
          fields("invocation")),
          fields("kind").convertTo[String],
          fields("sid").convertTo[Int],
          fields("value").convertTo[String])
        case "return" =>
          new ReturnEvent(InvocationJsonFormat.read(
            fields("invocation")),
            fields("kind").convertTo[String],
            fields("sid").convertTo[Int],
            "NoReturnVal")
        case _ => throw new Exception("Wrong arg in kind in Event json")
      }

    }
  }

  implicit object HistoryJsonFormat extends RootJsonFormat[History] {

    def write(h: History): JsObject = JsObject(
      "schema" -> SchemaJsonFormat.write(h.schema),
      "events" -> JsArray(h.events.map(_.toJson).toVector),
      "frequency" -> FrequencyJsonFormat.write(h.frequency))

    def read(json: JsValue): History = {
      val fields = json.asJsObject.fields
      History(
        SchemaJsonFormat.read(fields("schema")),
        fields("events").asInstanceOf[JsArray].elements.map(_.asInstanceOf[JsObject]).map(EventJsonFormat.read(_)).toList,
        FrequencyJsonFormat.read(fields("frequency")))
    }
  }

  // https://groups.google.com/forum/#!topic/spray-user/zZl_LbH8fN8
  implicit object AnyJsonFormat extends JsonFormat[Any] {
    def write(x: Any) = x match {
      case n: Int => JsNumber(n)
      case s: String => JsString(s)
      case x: Seq[_] => seqFormat[Any].write(x)
      case m: Map[String, Int] => mapFormat[String, Int].write(m)
      case b: Boolean if b == true => JsTrue
      case b: Boolean if b == false => JsFalse
      case x => serializationError("Do not understand object of type " + x.getClass.getName)
    }
    def read(value: JsValue) = value match {
      case JsNumber(n) => n.intValue()
      case JsString(s) => s
      case a: JsArray => listFormat[Any].read(value)
      case o: JsObject => mapFormat[String, Any].read(value)
      case JsTrue => true
      case JsFalse => false
      case x => deserializationError("Do not understand how to deserialize " + x)
    }
  }


}

