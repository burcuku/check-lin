package lin.testGen


import java.io.File
import java.util.concurrent.atomic.AtomicInteger

import com.github.javaparser.ast.{CompilationUnit, Modifier, NodeList}
import com.github.javaparser.ast.`type`.{ClassOrInterfaceType, PrimitiveType}
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr._
import com.github.javaparser.ast.stmt._
import lin.TestConfig
import lin.scheduleGen.json.Invocation
import lin.scheduleGen.Operation

/**
  *
  * @param schedules The list of schedules for each of which a test method will be generated in the test class
  * @param expectedResult The list of expected results - ordered by the operation id
  * @param testedClassName Name of the tested class (e.g. java.util.concurrent.ConcurrentLinkedQueue<Integer>)
  * @param imports The list of library classes to be imported in the generated test class
  * @param generatedPackageName The package name of the generated test class
  * @param generatedClassName The name of the generated test class
  * @param methodNamePrefix The prefix for the method names in the test class (each method corresponds to a schedule)
  */
class TestWriter(val testConfig: TestConfig,
                 val schedules: Seq[Seq[Operation]],
                 val expectedResult: Seq[String], // result of each operation ordered by their ids
                 val generatedPackageName: String = TestConfig.generatedPckName,
                 val generatedClassName: String = TestConfig.generatedClassName,
                 val methodNamePrefix: String = TestConfig.testedMethodPrefix,
                 val producedFromHistoryFile: String = "Not Specified")
{
  val testedClassName: String = testConfig.classUnderTest
  val imports: Seq[String] = testConfig.imports

  val expectedPairs: Map[Int, String] = expectedResult.indices.zip(expectedResult).toMap
  val allImports: List[String] = "java.util.Iterator" :: ("java.util.Arrays" ::  ("java.util.ArrayList" :: imports.toList)) //adds imports used by generated code

  // constant parameters:
  val resultType: String = "java.util.ArrayList<String>"
  val resultVarName: String = "result"
  val expectedResultVarName: String = "expectedResult"
  val resultToStrMethodName: String = "resultToStr"
  val iteratorToStrMethodName: String = "iteratorToStr"
  val testedObjectVarName: String = "object"

  private val counter = new AtomicInteger(0)

  private val cu = new CompilationUnit
  cu.setPackageDeclaration(generatedPackageName)
  allImports.foreach(i => cu.addImport(i))

  // create the type declaration
  private val ttype = cu.addClass(generatedClassName)

  createClass()

  private def createClass() = {
    schedules.zip(1 to schedules.size).foreach(pair => callSchedule(pair._1, methodNamePrefix + pair._2))
    addIteratorToStrMethod(iteratorToStrMethodName)
    addResultToStringMethod(resultToStrMethodName)
    addMainMethod()
  }

  private def addMainMethod() = {
    val numCalls = schedules.size
    val mainMethod = ttype.addMethod("main", Modifier.PUBLIC, Modifier.STATIC)
    mainMethod.addAndGetParameter(classOf[String], "args").setVarArgs(true)
    val block = new BlockStmt

    val clazz = new NameExpr("System")
    val field = new FieldAccessExpr(clazz, "out")
    val historyFNamePrinter = new MethodCallExpr(field, "println")
    historyFNamePrinter.addArgument(new StringLiteralExpr(
      "\nHistory from file: " + producedFromHistoryFile.replace("\\", "\\\\")))
    block.addStatement(historyFNamePrinter)

    val testNamePrinter = new MethodCallExpr(field, "println")
    testNamePrinter.addArgument(new StringLiteralExpr(
      "Running test file: " + Utils.getTestFileName(generatedPackageName, generatedClassName)
        .replace("\\", "\\\\").concat("\n")))
    block.addStatement(testNamePrinter)

    (1 to numCalls).foreach(i => {
      // In the then block, print it is linearizable and return!
      val thenBlock = new BlockStmt
      val resPrinter = new MethodCallExpr(field, "println")
      var scheduleStr = ""
      schedules(i-1).foreach(s => {
        var argStr = ""
        s.callEvent.event.invocation.arguments.foreach(a => argStr = argStr.concat(a.toString))
        scheduleStr = scheduleStr.concat(s.callEvent.event.invocation.method.name + argStr + "\n")
      })
      val strRes = if(generatedClassName.contains("Part")) "\nPart Linearizable with schedule: " else "\nLinearizable with schedule: "
      resPrinter.addArgument(new StringLiteralExpr(strRes + i
        + "\n" + scheduleStr.concat("\n")))
      thenBlock.addStatement(resPrinter)
      thenBlock.addStatement(new ReturnStmt())

      val ifStatement = new IfStmt(new MethodCallExpr(methodNamePrefix + i), thenBlock, null)
      block.addStatement(ifStatement)
    })

    val strRes = if(generatedClassName.contains("Part")) "\nPart is not found to be linearizable!" else "\nNot linearizable!"

    // If none of the if branches are taken, the schedule is not linearizable!
    val resprinter = new MethodCallExpr(field, "println")
    resprinter.addArgument(new StringLiteralExpr(strRes))
    block.addStatement(resprinter)

    mainMethod.setBody(block)
  }

  private def callSchedule(schedule: Seq[Operation], methodName: String) =
    callInvocations(schedule.map(_.callEvent.event.invocation), methodName)

  private def callInvocations(invocations: Seq[Invocation], methodName: String) = {
    // create a method
    val method1 = ttype.addMethod(methodName, Modifier.PUBLIC, Modifier.STATIC)
    method1.setType(PrimitiveType.booleanType())
    // add a body to the method
    val block = new BlockStmt

    // for printing:
    val systemClazz = new NameExpr("System")
    val outField = new FieldAccessExpr(systemClazz, "out")

    if(TestConfig.printSchedulesInTests) {
      // print schedule:
      val schedule: String = invocations.map(_.id).foldLeft(new StringBuilder) { (sb, s) => sb append s append " " }.toString
      val printExpr1 = new MethodCallExpr(outField, "println")
      printExpr1.addArgument(new StringLiteralExpr("Schedule by method ids: " + schedule))
      block.addStatement(printExpr1)
    }

    // set expected results:
    val expected: Seq[String] = invocations.map(inv => expectedPairs(inv.id))

    val assignExpectedExpr = new AssignExpr
    val varExpr = new VariableDeclarationExpr(new ClassOrInterfaceType("java.util.List"), expectedResultVarName)
    assignExpectedExpr.setTarget(varExpr)
    val clazz = new NameExpr("Arrays")
    val resultsArrayExpr = new MethodCallExpr(clazz, "asList")
    expected.foreach(value => resultsArrayExpr.addArgument(new StringLiteralExpr(value)))
    assignExpectedExpr.setValue(resultsArrayExpr)

    block.addStatement(assignExpectedExpr)
    createObject(block, resultType, resultVarName)

    if(TestConfig.printSchedulesInTests) {
      // print expected results
      val printExpr2 = new MethodCallExpr(outField, "println")
      printExpr2.addArgument(new StringLiteralExpr("Expected results: "))
      block.addStatement(printExpr2)
      val printExpr3 = new MethodCallExpr(outField, "println")
      printExpr3.addArgument(new NameExpr(expectedResultVarName))
      block.addStatement(printExpr3)
    }

    // str to be used to keep the return value of a method
    block.addStatement(new VariableDeclarationExpr(
      new ClassOrInterfaceType("java.lang.String"), "strOrEx"))

    val testObjectExpr = createObject(block, testedClassName, testedObjectVarName)
    val testObjectName = testObjectExpr.getVariable(0).getName.asString

    // call methods:
    invocations.foreach(i => callMethod(testObjectName, i).foreach(block.addStatement))

    // call equals on the values
    val equalsExpr = new MethodCallExpr(new NameExpr(expectedResultVarName), "equals")
    equalsExpr.addArgument(new NameExpr(resultVarName))

    if(TestConfig.printSchedulesInTests) {
      // print results
      val printExpr4 = new MethodCallExpr(outField, "println")
      printExpr4.addArgument(new StringLiteralExpr("Obtained results: "))
      block.addStatement(printExpr4)
      val printExpr5 = new MethodCallExpr(outField, "println")
      printExpr5.addArgument(new NameExpr(resultVarName))
      block.addStatement(printExpr5)
    }

    // return result variable
    block.addStatement(new ReturnStmt(equalsExpr))

    method1.setBody(block)
  }

  private def callMethod(objectName: String, invocation: Invocation): Seq[Statement] = {
    val call = new MethodCallExpr(new NameExpr(objectName), invocation.method.name)
    val pairForEachArg: Seq[(Seq[Expression], Seq[Expression])] = invocation.arguments.map(a => Utils.intoExpression(a, testConfig.collParamType, testConfig.collParamMethod))

    pairForEachArg.flatMap(_._2).foreach(a => call.addArgument(a))

    // stmts for creating objects for each arg
    val objectExprs: Seq[Expression] = pairForEachArg.flatMap(_._1)

    val clazz = new NameExpr("String")
    val stringValueCall = new MethodCallExpr(clazz, "valueOf")

    val callForString =
      // if the return value of the called method is void, no return value is expected
    if(Utils.isVoid(testedClassName, invocation.method.name))
      stringValueCall.addArgument(new StringLiteralExpr("NoReturnVal"))
    else
    // call result to string
      new MethodCallExpr(resultToStrMethodName).addArgument(call)

    // for try block:
    val tryBlock = new BlockStmt()
    val tryBlockStmt = new AssignExpr()
    tryBlockStmt.setTarget(new NameExpr("strOrEx"))
    tryBlockStmt.setValue(callForString)
    tryBlock.addStatement(tryBlockStmt)
    // for catch block:
    val catchBlock = new BlockStmt()
    val catchBlockStmt = new AssignExpr()
    catchBlockStmt.setTarget(new NameExpr("strOrEx"))
    val exceptionStrCall = new MethodCallExpr(new MethodCallExpr(new MethodCallExpr(
      new NameExpr("e"), "getClass"), "toString"), "substring")
    exceptionStrCall.addArgument(new IntegerLiteralExpr(6)) // split "class"
    catchBlockStmt.setValue(exceptionStrCall)

    catchBlock.addStatement(catchBlockStmt)
    val catchClause = new CatchClause(new Parameter(new ClassOrInterfaceType("java.lang.Exception"), "e"), catchBlock)
    val nodeList = new NodeList[CatchClause]()
    nodeList.add(catchClause)
    // try stmt:
    val tryStmt = new TryStmt(tryBlock, nodeList, null)

    val addToResultExpr = new MethodCallExpr(new NameExpr(resultVarName), "add")
    addToResultExpr.addArgument(new NameExpr("strOrEx"))

    if(Utils.isVoid(testedClassName, invocation.method.name))
      objectExprs.map(new ExpressionStmt(_)) ++ List(new ExpressionStmt(call), tryStmt, new ExpressionStmt(addToResultExpr))
    else
      objectExprs.map(new ExpressionStmt(_)) ++ List(tryStmt, new ExpressionStmt(addToResultExpr))

  }

  private def createObject(block: BlockStmt, typeName: String, varName: String) = { // create an object of type CU
    val assignExpr = new AssignExpr
    val varExpr = new VariableDeclarationExpr(new ClassOrInterfaceType(typeName), varName)
    assignExpr.setTarget(varExpr)
    val callConst = new ObjectCreationExpr
    callConst.setType(typeName)

    if(typeName.equals("java.util.concurrent.ArrayBlockingQueue<Integer>"))
      callConst.addArgument(new IntegerLiteralExpr(100))

    assignExpr.setValue(callConst)
    block.addStatement(assignExpr)
    varExpr
  }

  private def addResultToStringMethod(methodName: String): Unit = {
    val method = ttype.addMethod(methodName, Modifier.PUBLIC, Modifier.STATIC)
    method.addAndGetParameter("java.lang.Object", "resultObject").setVarArgs(false)
    method.setType("java.lang.String")
    val block = new BlockStmt

    val objectExpr = new NameExpr("resultObject")

    // if the return value is of type iterator, convert each element to string before adding to result array
    val ifStmt1 = new IfStmt()
    ifStmt1.setCondition(new InstanceOfExpr(objectExpr, new ClassOrInterfaceType("java.util.Iterator")))
    val callExpr1 = new MethodCallExpr(iteratorToStrMethodName)
    callExpr1.addArgument(objectExpr)
    val returnStmt1 = new ReturnStmt(callExpr1)
    ifStmt1.setThenStmt(returnStmt1)
    block.addStatement(ifStmt1)

    // if the return value is of type object array, convert each element to string before adding to result array
    val ifStmt2 = new IfStmt()
    ifStmt2.setCondition(new InstanceOfExpr(objectExpr, new ClassOrInterfaceType("Object[]")))
    val arrayClazz = new NameExpr("Arrays")
    val toStreamCall = new MethodCallExpr(arrayClazz, "stream")
    toStreamCall.addArgument(new CastExpr(new ClassOrInterfaceType("Object[]"), objectExpr))
    val iteratorCall = new MethodCallExpr(toStreamCall, "iterator")
    val callExpr2 = new MethodCallExpr(iteratorToStrMethodName)
    callExpr2.addArgument(iteratorCall)
    val returnStmt2 = new ReturnStmt(callExpr2)
    ifStmt2.setThenStmt(returnStmt2)
    block.addStatement(ifStmt2)

    // add the string value of the returned value into the results array
    val clazz = new NameExpr("String")
    val callExpr3 = new MethodCallExpr(clazz, "valueOf")
    callExpr3.addArgument(objectExpr)
    val returnStmt3 = new ReturnStmt(callExpr3)
    block.addStatement(returnStmt3)

    method.setBody(block)
  }

  private def addIteratorToStrMethod(methodName: String): Unit = {
    val method = ttype.addMethod(methodName, Modifier.PUBLIC, Modifier.STATIC)
    method.addAndGetParameter("java.lang.Object", "object").setVarArgs(false)
    method.setType("java.lang.String")
    val block = new BlockStmt

    val assignCastExpr = new AssignExpr
    val varCastExpr = new VariableDeclarationExpr(new ClassOrInterfaceType("java.util.Iterator"), "iterator")
    assignCastExpr.setTarget(varCastExpr)
    val castExpr = new CastExpr(new ClassOrInterfaceType("java.util.Iterator"), new NameExpr("object"))
    assignCastExpr.setValue(castExpr)
    block.addStatement(assignCastExpr)

    // String s = "[";
    val assignExpr = new AssignExpr
    val varExpr = new VariableDeclarationExpr(new ClassOrInterfaceType("java.lang.String"), "s")
    assignExpr.setTarget(varExpr)
    val callConst = new ObjectCreationExpr
    callConst.setType("java.lang.String")
    callConst.addArgument(new StringLiteralExpr("["))
    assignExpr.setValue(callConst)
    block.addStatement(assignExpr)

    // while (iterator.hasNext()) s = s.concat(String.valueOf(iterator.next()));
    val whileStmt = new WhileStmt()
    val condExpr = new MethodCallExpr(new NameExpr("iterator"), "hasNext")
    whileStmt.setCondition(condExpr)

    val concatExpr = new MethodCallExpr(new NameExpr("s"), "concat")
    val nextExpr = new MethodCallExpr(new NameExpr("iterator"), "next")
    val stringClazz = new NameExpr("String")
    val valueOfExpr = new MethodCallExpr(stringClazz, "valueOf")
    valueOfExpr.addArgument(nextExpr)
    concatExpr.addArgument(valueOfExpr)
    val assignExpr2 = new AssignExpr
    assignExpr2.setTarget(new NameExpr("s"))
    assignExpr2.setValue(concatExpr)

    val ifStmt = new IfStmt()
    ifStmt.setCondition(condExpr)
    val assignExpr3 = new AssignExpr
    assignExpr3.setTarget(new NameExpr("s"))
    val concatExpr2 = new MethodCallExpr(new NameExpr("s"), "concat")
    concatExpr2.addArgument(new StringLiteralExpr(","))
    assignExpr3.setValue(concatExpr2)
    ifStmt.setThenStmt(new ExpressionStmt(assignExpr3))
    ifStmt.setElseStmt(null)

    whileStmt.setBody(new BlockStmt().addStatement(assignExpr2).addStatement(ifStmt))
    block.addStatement(whileStmt)

    //return s.concat("]");
    val concatExpr3 = new MethodCallExpr(new NameExpr("s"), "concat")
    concatExpr3.addArgument(new StringLiteralExpr("]"))
    block.addStatement(new ReturnStmt(concatExpr3))
    method.setBody(block)
  }

  override def toString: String = cu.toString()

  /**
    * Writes the generated test class into a Java file
    */
  def writeClass(): Unit = {
    val directoryName = generatedPackageName.replace(".", File.separator)
    Utils.writeToFile(directoryName, generatedClassName + ".java", toString, false)
  }
}
