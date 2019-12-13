package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class UnicodeIntervalElementAnalyzerTest {
    @Test
    fun `test single char`() {
        val left = 'a'
        val text = "$left"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalElementNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(1L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(left.toInt(), result.primitive.firstPoint, "The firstPoint property is incorrect")
        Assertions.assertEquals(left.toInt(), result.primitive.lastPoint, "The lastPoint property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test single expression`() {
        val left = 'a'.toInt()
        val text = "${EscapedExpressionNode.startToken}$left${EscapedExpressionNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalElementNode.Companion::parse)

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
    fun `test range char`() {
        val left = 'a'
        val right = 'x'
        val text = "$left${UnicodeIntervalElementNode.rangeToken}$right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalElementNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(right - left + 1L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(left.toInt(), result.primitive.firstPoint, "The firstPoint property is incorrect")
        Assertions.assertEquals(right.toInt(), result.primitive.lastPoint, "The lastPoint property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test range expression`() {
        val leftValue = 'a'.toInt()
        val rightValue = 'x'.toInt()
        val left = "${EscapedExpressionNode.startToken}$leftValue${EscapedExpressionNode.endToken}"
        val right = "${EscapedExpressionNode.startToken}$rightValue${EscapedExpressionNode.endToken}"
        val text = "$left${UnicodeIntervalElementNode.rangeToken}$right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalElementNode.Companion::parse)

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
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType) {
            val left = "${EscapedExpressionNode.startToken}${LogicNode.trueLiteral}${EscapedExpressionNode.endToken}"
            val right = 'g'
            val text = "$left${UnicodeIntervalElementNode.rangeToken}$right"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalElementNode.Companion::parse)

            // Prepare stack.
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test incorrect right`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType) {
            val left = 'g'
            val right = "${EscapedExpressionNode.startToken}${LogicNode.trueLiteral}${EscapedExpressionNode.endToken}"
            val text = "$left${UnicodeIntervalElementNode.rangeToken}$right"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalElementNode.Companion::parse)

            // Prepare stack.
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test left bigger than right`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncorrectRangeBounds) {
            val left = 'x'
            val right = 'a'
            val text = "$left${UnicodeIntervalElementNode.rangeToken}$right"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalElementNode.Companion::parse)

            // Prepare stack.
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.Empty)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
