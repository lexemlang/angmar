package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class IntervalElementAnalyzerTest {
    @Test
    fun `test single expression`() {
        val variableName = "testVariable"
        val left = 123
        val text = "${EscapedExpressionNode.startToken}$variableName${EscapedExpressionNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalElementNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, variableName, LxmInteger.from(left))

        // Prepare stack.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(1L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(left, result.primitive.firstPoint, "The firstPoint property is incorrect")
        Assertions.assertEquals(left, result.primitive.lastPoint, "The lastPoint property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableName))
    }

    @Test
    fun `test range expression`() {
        val variableLeftName = "testLeftVariable"
        val variableRightName = "testRightVariable"
        val leftValue = 123
        val rightValue = 456
        val left = "${EscapedExpressionNode.startToken}$variableLeftName${EscapedExpressionNode.endToken}"
        val right = "${EscapedExpressionNode.startToken}$variableRightName${EscapedExpressionNode.endToken}"
        val text = "$left${IntervalElementNode.rangeToken}$right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalElementNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, variableLeftName, LxmInteger.from(leftValue))
        context.setProperty(analyzer.memory, variableRightName, LxmInteger.from(rightValue))

        // Prepare stack.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(rightValue - leftValue + 1L, result.primitive.pointCount,
                "The pointCount property is incorrect")
        Assertions.assertEquals(leftValue, result.primitive.firstPoint, "The firstPoint property is incorrect")
        Assertions.assertEquals(rightValue, result.primitive.lastPoint, "The lastPoint property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(variableLeftName, variableRightName))
    }

    @Test
    @Incorrect
    fun `test incorrect left`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType) {
            val variableName = "testVariable"
            val left = "${EscapedExpressionNode.startToken}$variableName${EscapedExpressionNode.endToken}"
            val right = 456
            val text = "$left${IntervalElementNode.rangeToken}$right"
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalElementNode.Companion::parse)

            // Prepare context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            context.setProperty(analyzer.memory, variableName, LxmLogic.True)

            // Prepare stack.
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test incorrect right`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType) {
            val variableName = "testVariable"
            val left = 123
            val right = "${EscapedExpressionNode.startToken}$variableName${EscapedExpressionNode.endToken}"
            val text = "$left${IntervalElementNode.rangeToken}$right"
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalElementNode.Companion::parse)

            // Prepare context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            context.setProperty(analyzer.memory, variableName, LxmLogic.True)

            // Prepare stack.
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test left bigger than right`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncorrectRangeBounds) {
            val variableLeftName = "testLeftVariable"
            val variableRightName = "testRightVariable"
            val leftValue = 456
            val rightValue = 123
            val left = "${EscapedExpressionNode.startToken}$variableLeftName${EscapedExpressionNode.endToken}"
            val right = "${EscapedExpressionNode.startToken}$variableRightName${EscapedExpressionNode.endToken}"
            val text = "$left${UnicodeIntervalElementNode.rangeToken}$right"
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalElementNode.Companion::parse)

            // Prepare context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            context.setProperty(analyzer.memory, variableLeftName, LxmInteger.from(leftValue))
            context.setProperty(analyzer.memory, variableRightName, LxmInteger.from(rightValue))

            // Prepare stack.
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
