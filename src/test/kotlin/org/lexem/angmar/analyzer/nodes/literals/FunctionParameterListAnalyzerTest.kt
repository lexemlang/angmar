package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class FunctionParameterListAnalyzerTest {
    private val param1Id = "param1"
    private val param2Id = "param2"
    private val positionalSpreadId = "positionalSpread"
    private val namedSpreadId = "namedSpread"

    @Test
    fun `test params`() {
        val params =
                "$param1Id ${FunctionParameterListNode.parameterSeparator} $param2Id ${FunctionParameterNode.assignOperator} 1"
        val text = "${FunctionParameterListNode.startToken} $params ${FunctionParameterListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FunctionParameterListNode.Companion::parse)

        // Prepare stack.
        val arguments = LxmArguments(analyzer.memory)
        arguments.addPositionalArgument(LxmInteger.Num10)
        arguments.addNamedArgument("named1", LxmInteger.Num10)
        arguments.addNamedArgument(AnalyzerCommons.Identifiers.This, LxmLogic.True)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Arguments, arguments)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val param1 = context.getPropertyValue(analyzer.memory, param1Id)
        val param2 = context.getPropertyValue(analyzer.memory, param2Id)

        Assertions.assertEquals(LxmInteger.Num10, param1, "The $param1Id is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, param2, "The $param2Id is incorrect")

        Assertions.assertEquals(LxmLogic.True,
                context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.This),
                "The ${AnalyzerCommons.Identifiers.This} param is incorrect")

        val finalArguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments) as? LxmReference
        Assertions.assertEquals(arguments.getPrimitive().position, finalArguments?.position,
                "The arguments are incorrect")

        // Remove Arguments from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Arguments)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(param1Id, param2Id, AnalyzerCommons.Identifiers.This))
    }

    @Test
    fun `test positional`() {
        val positionalSpread = "${FunctionParameterListNode.positionalSpreadOperator}$positionalSpreadId"
        val text = "${FunctionParameterListNode.startToken} $positionalSpread ${FunctionParameterListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FunctionParameterListNode.Companion::parse)

        // Prepare stack.
        val arguments = LxmArguments(analyzer.memory)
        arguments.addPositionalArgument(LxmInteger.Num10)
        arguments.addNamedArgument("named1", LxmInteger.Num10)
        arguments.addNamedArgument(AnalyzerCommons.Identifiers.This, LxmLogic.True)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Arguments, arguments)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val positionalSpreadParam =
                context.getPropertyValue(analyzer.memory, positionalSpreadId)?.dereference(analyzer.memory,
                        toWrite = false) as? LxmList ?: throw Error("The positionalSpreadParam must be a LxmList")

        val positionalParams = positionalSpreadParam.getAllCells()
        Assertions.assertEquals(1, positionalParams.size, "The number of positional params is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, positionalParams[0], "The positionalParams[0] is incorrect")

        Assertions.assertEquals(LxmLogic.True,
                context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.This),
                "The ${AnalyzerCommons.Identifiers.This} param is incorrect")

        val finalArguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments) as? LxmReference
        Assertions.assertEquals(arguments.getPrimitive().position, finalArguments?.position,
                "The arguments are incorrect")

        // Remove Arguments from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Arguments)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(positionalSpreadId, AnalyzerCommons.Identifiers.This))
    }

    @Test
    fun `test named`() {
        val namedSpread = "${FunctionParameterListNode.namedSpreadOperator}$namedSpreadId"
        val text = "${FunctionParameterListNode.startToken} $namedSpread ${FunctionParameterListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FunctionParameterListNode.Companion::parse)

        // Prepare stack.
        val arguments = LxmArguments(analyzer.memory)
        arguments.addPositionalArgument(LxmInteger.Num10)
        arguments.addNamedArgument("named1", LxmInteger.Num10)
        arguments.addNamedArgument(AnalyzerCommons.Identifiers.This, LxmLogic.True)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Arguments, arguments)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val namedSpreadParam = context.getPropertyValue(analyzer.memory, namedSpreadId)?.dereference(analyzer.memory,
                toWrite = false) as? LxmObject ?: throw Error("The namedSpreadParam must be a LxmObject")

        val namedParams = namedSpreadParam.getAllIterableProperties()
        Assertions.assertEquals(1, namedParams.size, "The number of named params is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, namedParams["named1"]!!.value, "The named named1 param is incorrect")

        Assertions.assertEquals(LxmLogic.True,
                context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.This),
                "The ${AnalyzerCommons.Identifiers.This} param is incorrect")

        val finalArguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments) as? LxmReference
        Assertions.assertEquals(arguments.getPrimitive().position, finalArguments?.position,
                "The arguments are incorrect")

        // Remove Arguments from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Arguments)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(namedSpreadId, AnalyzerCommons.Identifiers.This))
    }

    @Test
    fun `test params-positional`() {
        val params =
                "$param1Id ${FunctionParameterListNode.parameterSeparator} $param2Id ${FunctionParameterNode.assignOperator} 1"
        val positionalSpread = "${FunctionParameterListNode.positionalSpreadOperator}$positionalSpreadId"
        val text =
                "${FunctionParameterListNode.startToken} $params ${FunctionParameterListNode.parameterSeparator} $positionalSpread ${FunctionParameterListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FunctionParameterListNode.Companion::parse)

        // Prepare stack.
        val arguments = LxmArguments(analyzer.memory)
        arguments.addPositionalArgument(LxmInteger.Num10)
        arguments.addNamedArgument("named1", LxmInteger.Num10)
        arguments.addNamedArgument(AnalyzerCommons.Identifiers.This, LxmLogic.True)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Arguments, arguments)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val param1 = context.getPropertyValue(analyzer.memory, "param1")
        val param2 = context.getPropertyValue(analyzer.memory, "param2")
        val positionalSpreadParam =
                context.getPropertyValue(analyzer.memory, positionalSpreadId)?.dereference(analyzer.memory,
                        toWrite = false) as? LxmList ?: throw Error("The positionalSpreadParam must be a LxmList")

        Assertions.assertEquals(LxmInteger.Num10, param1, "The param1 is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, param2, "The param2 is incorrect")

        val positionalParams = positionalSpreadParam.getAllCells()
        Assertions.assertEquals(0, positionalParams.size, "The number of positional params is incorrect")

        Assertions.assertEquals(LxmLogic.True,
                context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.This),
                "The ${AnalyzerCommons.Identifiers.This} param is incorrect")

        val finalArguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments) as? LxmReference
        Assertions.assertEquals(arguments.getPrimitive().position, finalArguments?.position,
                "The arguments are incorrect")

        // Remove Arguments from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Arguments)

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(param1Id, param2Id, positionalSpreadId, AnalyzerCommons.Identifiers.This))
    }

    @Test
    fun `test params-named`() {
        val params =
                "param1 ${FunctionParameterListNode.parameterSeparator} param2 ${FunctionParameterNode.assignOperator} 1"
        val namedSpread = "${FunctionParameterListNode.namedSpreadOperator}$namedSpreadId"
        val text =
                "${FunctionParameterListNode.startToken} $params ${FunctionParameterListNode.parameterSeparator} $namedSpread ${FunctionParameterListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FunctionParameterListNode.Companion::parse)

        // Prepare stack.
        val arguments = LxmArguments(analyzer.memory)
        arguments.addPositionalArgument(LxmInteger.Num10)
        arguments.addNamedArgument("named1", LxmInteger.Num10)
        arguments.addNamedArgument(AnalyzerCommons.Identifiers.This, LxmLogic.True)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Arguments, arguments)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val param1 = context.getPropertyValue(analyzer.memory, "param1")
        val param2 = context.getPropertyValue(analyzer.memory, "param2")
        val namedSpreadParam = context.getPropertyValue(analyzer.memory, namedSpreadId)?.dereference(analyzer.memory,
                toWrite = false) as? LxmObject ?: throw Error("The namedSpreadParam must be a LxmObject")

        Assertions.assertEquals(LxmInteger.Num10, param1, "The param1 is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, param2, "The param2 is incorrect")

        val namedParams = namedSpreadParam.getAllIterableProperties()
        Assertions.assertEquals(1, namedParams.size, "The number of named params is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, namedParams["named1"]!!.value, "The named named1 param is incorrect")

        Assertions.assertEquals(LxmLogic.True,
                context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.This),
                "The ${AnalyzerCommons.Identifiers.This} param is incorrect")

        val finalArguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments) as? LxmReference
        Assertions.assertEquals(arguments.getPrimitive().position, finalArguments?.position,
                "The arguments are incorrect")

        // Remove Arguments from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Arguments)

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(param1Id, param2Id, namedSpreadId, AnalyzerCommons.Identifiers.This))
    }

    @Test
    fun `test positional-named`() {
        val positionalSpread = "${FunctionParameterListNode.positionalSpreadOperator}$positionalSpreadId"
        val namedSpread = "${FunctionParameterListNode.namedSpreadOperator}$namedSpreadId"
        val text =
                "${FunctionParameterListNode.startToken} $positionalSpread ${FunctionParameterListNode.parameterSeparator} $namedSpread ${FunctionParameterListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FunctionParameterListNode.Companion::parse)

        // Prepare stack.
        val arguments = LxmArguments(analyzer.memory)
        arguments.addPositionalArgument(LxmInteger.Num10)
        arguments.addNamedArgument("named1", LxmInteger.Num10)
        arguments.addNamedArgument(AnalyzerCommons.Identifiers.This, LxmLogic.True)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Arguments, arguments)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val positionalSpreadParam =
                context.getPropertyValue(analyzer.memory, positionalSpreadId)?.dereference(analyzer.memory,
                        toWrite = false) as? LxmList ?: throw Error("The positionalSpreadParam must be a LxmList")
        val namedSpreadParam = context.getPropertyValue(analyzer.memory, namedSpreadId)?.dereference(analyzer.memory,
                toWrite = false) as? LxmObject ?: throw Error("The namedSpreadParam must be a LxmObject")

        val positionalParams = positionalSpreadParam.getAllCells()
        Assertions.assertEquals(1, positionalParams.size, "The number of positional params is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, positionalParams[0], "The positionalParams[0] is incorrect")

        val namedParams = namedSpreadParam.getAllIterableProperties()
        Assertions.assertEquals(1, namedParams.size, "The number of named params is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, namedParams["named1"]!!.value, "The named named1 param is incorrect")

        Assertions.assertEquals(LxmLogic.True,
                context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.This),
                "The ${AnalyzerCommons.Identifiers.This} param is incorrect")

        val finalArguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments) as? LxmReference
        Assertions.assertEquals(arguments.getPrimitive().position, finalArguments?.position,
                "The arguments are incorrect")

        // Remove Arguments from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Arguments)

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(positionalSpreadId, namedSpreadId, AnalyzerCommons.Identifiers.This))
    }

    @Test
    fun `test params-positional-named`() {
        val params =
                "$param1Id ${FunctionParameterListNode.parameterSeparator} $param2Id ${FunctionParameterNode.assignOperator} 1"
        val positionalSpread = "${FunctionParameterListNode.positionalSpreadOperator}$positionalSpreadId"
        val namedSpread = "${FunctionParameterListNode.namedSpreadOperator}$namedSpreadId"
        val text =
                "${FunctionParameterListNode.startToken} $params ${FunctionParameterListNode.parameterSeparator} $positionalSpread ${FunctionParameterListNode.parameterSeparator} $namedSpread ${FunctionParameterListNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FunctionParameterListNode.Companion::parse)

        // Prepare stack.
        val arguments = LxmArguments(analyzer.memory)
        arguments.addPositionalArgument(LxmInteger.Num10)
        arguments.addNamedArgument("named1", LxmInteger.Num10)
        arguments.addNamedArgument(AnalyzerCommons.Identifiers.This, LxmLogic.True)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Arguments, arguments)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val param1 = context.getPropertyValue(analyzer.memory, param1Id)
        val param2 = context.getPropertyValue(analyzer.memory, param2Id)
        val positionalSpreadParam =
                context.getPropertyValue(analyzer.memory, positionalSpreadId)?.dereference(analyzer.memory,
                        toWrite = false) as? LxmList ?: throw Error("The positionalSpreadParam must be a LxmList")
        val namedSpreadParam = context.getPropertyValue(analyzer.memory, namedSpreadId)?.dereference(analyzer.memory,
                toWrite = false) as? LxmObject ?: throw Error("The namedSpreadParam must be a LxmObject")

        Assertions.assertEquals(LxmInteger.Num10, param1, "The $param1Id is incorrect")
        Assertions.assertEquals(LxmInteger.Num1, param2, "The $param2Id is incorrect")

        val positionalParams = positionalSpreadParam.getAllCells()
        Assertions.assertEquals(0, positionalParams.size, "The number of positional params is incorrect")

        val namedParams = namedSpreadParam.getAllIterableProperties()
        Assertions.assertEquals(1, namedParams.size, "The number of named params is incorrect")
        Assertions.assertEquals(LxmInteger.Num10, namedParams["named1"]!!.value, "The named named1 param is incorrect")

        Assertions.assertEquals(LxmLogic.True,
                context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.This),
                "The ${AnalyzerCommons.Identifiers.This} param is incorrect")

        val finalArguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments) as? LxmReference
        Assertions.assertEquals(arguments.getPrimitive().position, finalArguments?.position,
                "The arguments are incorrect")

        // Remove Arguments from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Arguments)

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(param1Id, param2Id, positionalSpreadId, namedSpreadId, AnalyzerCommons.Identifiers.This))
    }
}
