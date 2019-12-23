package org.lexem.angmar.compiler.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class IntervalElementCompiledTest {
    @Test
    fun `test single number`() {
        val left = 123
        val text = "$left"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalElementNode.Companion::parse)

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

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test single expression`() {
        val left = 123
        val text = "${EscapedExpressionNode.startToken}$left${EscapedExpressionNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalElementNode.Companion::parse)

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

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test range number`() {
        val left = 123
        val right = 456
        val text = "$left${IntervalElementNode.rangeToken}$right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalElementNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(right - left + 1L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(left, result.primitive.firstPoint, "The firstPoint property is incorrect")
        Assertions.assertEquals(right, result.primitive.lastPoint, "The lastPoint property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test range expression`() {
        val leftValue = 123
        val rightValue = 456
        val left = "${EscapedExpressionNode.startToken}$leftValue${EscapedExpressionNode.endToken}"
        val right = "${EscapedExpressionNode.startToken}$rightValue${EscapedExpressionNode.endToken}"
        val text = "$left${IntervalElementNode.rangeToken}$right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalElementNode.Companion::parse)

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

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    @Incorrect
    fun `test incorrect left`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.IncompatibleType) {
            val left = "${EscapedExpressionNode.startToken}${LogicNode.trueLiteral}${EscapedExpressionNode.endToken}"
            val right = 456
            val text = "$left${IntervalElementNode.rangeToken}$right"
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalElementNode.Companion::parse)

            // Prepare stack.
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test incorrect right`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.IncompatibleType) {
            val left = 123
            val right = "${EscapedExpressionNode.startToken}${LogicNode.trueLiteral}${EscapedExpressionNode.endToken}"
            val text = "$left${IntervalElementNode.rangeToken}$right"
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalElementNode.Companion::parse)

            // Prepare stack.
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test left bigger than right`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.IncorrectRangeBounds) {
            val left = 456
            val right = 123
            val text = "$left${IntervalElementNode.rangeToken}$right"
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalElementNode.Companion::parse)

            // Prepare stack.
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
