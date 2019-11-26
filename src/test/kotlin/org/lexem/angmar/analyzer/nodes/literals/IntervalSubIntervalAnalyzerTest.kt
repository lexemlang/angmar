package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.data.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class IntervalSubIntervalAnalyzerTest {
    @Test
    fun `test add`() {
        val text =
                "${IntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.Add.operator}2 4 6${IntervalSubIntervalNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        var initial = IntegerInterval.new(IntegerRange.new(3))
        initial += 7
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(5L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeAtOrNull(0)?.from, "The range[0].from property is incorrect")
        Assertions.assertEquals(4, result.primitive.rangeAtOrNull(0)?.to, "The range[0].to property is incorrect")
        Assertions.assertEquals(6, result.primitive.rangeAtOrNull(1)?.from, "The range[1].from property is incorrect")
        Assertions.assertEquals(7, result.primitive.rangeAtOrNull(1)?.to, "The range[1].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test sub`() {
        val text =
                "${IntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.Sub.operator}2 4 6${IntervalSubIntervalNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        var initial = IntegerInterval.new(IntegerRange.new(2))
        initial += 7
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(1L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(1, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals(7, result.primitive.rangeAtOrNull(0)?.from, "The range[0].from property is incorrect")
        Assertions.assertEquals(7, result.primitive.rangeAtOrNull(0)?.to, "The range[0].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test commons`() {
        val text =
                "${IntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.Common.operator}2 4 6${IntervalSubIntervalNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        val initial = IntegerInterval.new(IntegerRange.new(4, 6))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(2L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals(4, result.primitive.rangeAtOrNull(0)?.from, "The range[0].from property is incorrect")
        Assertions.assertEquals(4, result.primitive.rangeAtOrNull(0)?.to, "The range[0].to property is incorrect")
        Assertions.assertEquals(6, result.primitive.rangeAtOrNull(1)?.from, "The range[1].from property is incorrect")
        Assertions.assertEquals(6, result.primitive.rangeAtOrNull(1)?.to, "The range[1].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test not commons`() {
        val text =
                "${IntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.NotCommon.operator}2 4 6${IntervalSubIntervalNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        val initial = IntegerInterval.new(IntegerRange.new(4, 6))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(2L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeAtOrNull(0)?.from, "The range[0].from property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeAtOrNull(0)?.to, "The range[0].to property is incorrect")
        Assertions.assertEquals(5, result.primitive.rangeAtOrNull(1)?.from, "The range[1].from property is incorrect")
        Assertions.assertEquals(5, result.primitive.rangeAtOrNull(1)?.to, "The range[1].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test reverse add`() {
        val text =
                "${IntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.Add.operator}${IntervalSubIntervalNode.reversedToken}5${IntervalSubIntervalNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        val initial = IntegerInterval.new(IntegerRange.new(3))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(Int.MAX_VALUE.toLong(), result.primitive.pointCount,
                "The pointCount property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals(0, result.primitive.rangeAtOrNull(0)?.from, "The range[0].from property is incorrect")
        Assertions.assertEquals(4, result.primitive.rangeAtOrNull(0)?.to, "The range[0].to property is incorrect")
        Assertions.assertEquals(6, result.primitive.rangeAtOrNull(1)?.from, "The range[1].from property is incorrect")
        Assertions.assertEquals(Int.MAX_VALUE, result.primitive.rangeAtOrNull(1)?.to,
                "The range[1].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test reverse sub`() {
        val text =
                "${IntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.Sub.operator}${IntervalSubIntervalNode.reversedToken}5${IntervalSubIntervalNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        val initial = IntegerInterval.new(IntegerRange.new(3, 50))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(1, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(1, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals(5, result.primitive.rangeAtOrNull(0)?.from, "The range[0].from property is incorrect")
        Assertions.assertEquals(5, result.primitive.rangeAtOrNull(0)?.to, "The range[0].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test reverse common`() {
        val text =
                "${IntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.Common.operator}${IntervalSubIntervalNode.reversedToken}5${IntervalSubIntervalNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        val initial = IntegerInterval.new(IntegerRange.new(3, 50))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(50 - 6 + 4 - 3 + 2, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals(3, result.primitive.rangeAtOrNull(0)?.from, "The range[0].from property is incorrect")
        Assertions.assertEquals(4, result.primitive.rangeAtOrNull(0)?.to, "The range[0].to property is incorrect")
        Assertions.assertEquals(6, result.primitive.rangeAtOrNull(1)?.from, "The range[1].from property is incorrect")
        Assertions.assertEquals(50, result.primitive.rangeAtOrNull(1)?.to, "The range[1].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test reverse not common`() {
        val text =
                "${IntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.NotCommon.operator}${IntervalSubIntervalNode.reversedToken}5${IntervalSubIntervalNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        val initial = IntegerInterval.new(IntegerRange.new(3, 50))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(Int.MAX_VALUE - 51L + 2 + 3, result.primitive.pointCount,
                "The pointCount property is incorrect")
        Assertions.assertEquals(3, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals(0, result.primitive.rangeAtOrNull(0)?.from, "The range[0].from property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeAtOrNull(0)?.to, "The range[0].to property is incorrect")
        Assertions.assertEquals(5, result.primitive.rangeAtOrNull(1)?.from, "The range[1].from property is incorrect")
        Assertions.assertEquals(5, result.primitive.rangeAtOrNull(1)?.to, "The range[1].to property is incorrect")
        Assertions.assertEquals(51, result.primitive.rangeAtOrNull(2)?.from, "The range[2].from property is incorrect")
        Assertions.assertEquals(Int.MAX_VALUE, result.primitive.rangeAtOrNull(2)?.to,
                "The range[2].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
