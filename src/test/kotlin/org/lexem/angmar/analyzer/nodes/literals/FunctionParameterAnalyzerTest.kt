package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class FunctionParameterAnalyzerTest {
    @Test
    fun `test without expression`() {
        val parameterName = "id"
        val text = parameterName
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FunctionParameterNode.Companion::parse)

        // Prepare stack.
        val parameters = LxmParameters()
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Parameters, parameters)

        TestUtils.processAndCheckEmpty(analyzer)

        val parameterList = parameters.getParameters()
        Assertions.assertNull(parameters.namedSpread, "The namedSpread property is incorrect")
        Assertions.assertNull(parameters.positionalSpread, "The positionalSpread property is incorrect")
        Assertions.assertEquals(1, parameterList.size, "The number of parameters is incorrect")
        Assertions.assertEquals(parameterName, parameterList[0], "The first parameter is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        Assertions.assertEquals(LxmNil, context.getPropertyValue(analyzer.memory, parameterName),
                "The result is incorrect")

        Assertions.assertEquals(parameters, analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Parameters),
                "The parameters is not saved again")

        // Remove Parameters from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Parameters)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(parameterName))
    }

    @Test
    fun `test with expression`() {
        val parameterName = "id"
        val value = LxmInteger.Num10
        val text = "$parameterName ${FunctionParameterNode.assignOperator} $value"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FunctionParameterNode.Companion::parse)

        // Prepare stack.
        val parameters = LxmParameters()
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Parameters, parameters)

        TestUtils.processAndCheckEmpty(analyzer)

        val parameterList = parameters.getParameters()
        Assertions.assertNull(parameters.namedSpread, "The namedSpread property is incorrect")
        Assertions.assertNull(parameters.positionalSpread, "The positionalSpread property is incorrect")
        Assertions.assertEquals(1, parameterList.size, "The number of parameters is incorrect")
        Assertions.assertEquals(parameterName, parameterList[0], "The first parameter is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        Assertions.assertEquals(value, context.getPropertyValue(analyzer.memory, parameterName),
                "The result is incorrect")

        Assertions.assertEquals(parameters, analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Parameters),
                "The parameters is not saved again")

        // Remove Parameters from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Parameters)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(parameterName))
    }

    @Test
    @Incorrect
    fun `test incorrect identifier`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType) {
            val text = "${EscapedExpressionNode.startToken}${LogicNode.trueLiteral}${EscapedExpressionNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FunctionParameterNode.Companion::parse)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
