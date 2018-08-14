package lin.testGen

import java.io.{BufferedWriter, File, FileWriter}
import java.util.concurrent.atomic.AtomicInteger

import com.github.javaparser.ast.`type`.ClassOrInterfaceType
import com.github.javaparser.ast.expr._
import lin.TestConfig
import lin.scheduleGen.json._

object Utils {

  var varNameCounter = new AtomicInteger(1)
  val collParamName = TestConfig.collParamName
  val collParamType = TestConfig.collParamType
  val collParamMethod = TestConfig.collParamMethod

  def getTestFileName(packageName: String, fileName: String): String =
    packageName.replace(".", File.separator) + File.separator + fileName + ".java"

  def writeToFile(directoryName: String, fileName: String, content: String, toAppend: Boolean): Unit = {
    val dirName = directoryName.replace(".", File.separator)
    val directory = new File(dirName)
    if (!directory.exists) directory.mkdirs

    val fw = new FileWriter(dirName + File.separator + fileName, true) ;
    fw.write(content) ;
    fw.close()
  }

  // returns the stmts to add before the call and the arguments to add into a call
  def intoExpression(arg: Argument): (Seq[Expression], Seq[Expression]) = arg match {
    case IntegerArgument(arg1) => (List(), List(new IntegerLiteralExpr(arg1)))

    case TwoIntegersArgument(arg1, arg2) => //e.g. for putAll(Map(1, 2))
      val assignExpr = new AssignExpr
      val varExpr = new VariableDeclarationExpr(new ClassOrInterfaceType(collParamType), collParamName + varNameCounter.getAndIncrement())
      assignExpr.setTarget(varExpr)
      val callConst = new ObjectCreationExpr
      callConst.setType(collParamType)

      if(collParamType.equals("java.util.concurrent.ArrayBlockingQueue<Integer>"))
        callConst.addArgument(new IntegerLiteralExpr(100))

      assignExpr.setValue(callConst)

      var stmts: List[Expression] = List(assignExpr)

      val varNameStr = varExpr.getVariable(0).getName.asString
      val methodCallExpr = new MethodCallExpr(new NameExpr(varNameStr), collParamMethod)
      methodCallExpr.addArgument(new IntegerLiteralExpr(arg1))
      methodCallExpr.addArgument(new IntegerLiteralExpr(arg2))
      stmts = stmts ++ List(methodCallExpr)

      (stmts, List(new NameExpr(varNameStr)))

    case SequenceArgument(arg: Seq[Integer]) => //e.g. for retainAll(List(1, 2))
      val assignExpr = new AssignExpr
      val varExpr = new VariableDeclarationExpr(new ClassOrInterfaceType(collParamType), collParamName + varNameCounter.getAndIncrement())
      assignExpr.setTarget(varExpr)
      val callConst = new ObjectCreationExpr
      callConst.setType(collParamType)

      if(collParamType.equals("java.util.concurrent.ArrayBlockingQueue<Integer>"))
        callConst.addArgument(new IntegerLiteralExpr(100))

      assignExpr.setValue(callConst)

      var stmts: List[Expression] = List(assignExpr)

      val varNameStr = varExpr.getVariable(0).getName.asString
      arg.foreach(a => {
        val methodCallExpr = new MethodCallExpr(new NameExpr(varNameStr), collParamMethod)
        methodCallExpr.addArgument(new IntegerLiteralExpr(a))
        stmts = stmts ++ List(methodCallExpr)
      })

      (stmts, List(new NameExpr(varNameStr)))

    case MapArgument(arg: Map[String, Int]) => //e.g. for retainAll(List(1, 2))
      val assignExpr = new AssignExpr
      val varExpr = new VariableDeclarationExpr(new ClassOrInterfaceType(collParamType), collParamName + varNameCounter.getAndIncrement())
      assignExpr.setTarget(varExpr)
      val callConst = new ObjectCreationExpr
      callConst.setType(collParamType)
      if(collParamType.equals("java.util.concurrent.ArrayBlockingQueue<Integer>"))
        callConst.addArgument(new IntegerLiteralExpr(100))

      assignExpr.setValue(callConst)

      var stmts: List[Expression] = List(assignExpr)

      val varNameStr = varExpr.getVariable(0).getName.asString
      arg.keys.foreach(a => {
        val methodCallExpr = new MethodCallExpr(new NameExpr(varNameStr), collParamMethod)
        methodCallExpr.addArgument(new IntegerLiteralExpr(a.toString))
        val value = arg.get(a)
        if(value.isDefined)
          methodCallExpr.addArgument(new IntegerLiteralExpr(value.get))
        else
          throw new Exception("Non-matching map in the argument!")
        stmts = stmts ++ List(methodCallExpr)
      })

      (stmts, List(new NameExpr(varNameStr)))

    }

  def isVoid(className: String, methodName: String): Boolean = {
    try {
      val methods = Class.forName(className.split("<")(0)).getMethods
      methods.find(m => m.getName.equals(methodName)).get.getReturnType.equals(Void.TYPE)
    } catch {
      case e: ClassNotFoundException =>
        println(e + " Cannot find class: " + className)
        false
      case e: Exception =>
        println(e + " Cannot find method: " + methodName + " in class: " + className.split("<")(0))
        false
    }
  }
}
