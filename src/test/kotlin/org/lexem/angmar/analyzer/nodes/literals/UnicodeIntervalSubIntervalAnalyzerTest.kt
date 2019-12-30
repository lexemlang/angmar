package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.data.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

// TODO make this tests with variables
@Disabled
internal class UnicodeIntervalSubIntervalAnalyzerTest {
    @Test
    fun `test add`() {
        val text =
                "${UnicodeIntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.Add.operator}a${UnicodeIntervalSubIntervalNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        val initial = IntegerInterval.new(IntegerRange.new(3))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(2L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals(3, result.primitive.rangeAtOrNull(0)?.from, "The range[0].from property is incorrect")
        Assertions.assertEquals(3, result.primitive.rangeAtOrNull(0)?.to, "The range[0].to property is incorrect")
        Assertions.assertEquals('a'.toInt(), result.primitive.rangeAtOrNull(1)?.from,
                "The range[1].from property is incorrect")
        Assertions.assertEquals('a'.toInt(), result.primitive.rangeAtOrNull(1)?.to,
                "The range[1].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test sub`() {
        val text =
                "${UnicodeIntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.Sub.operator}a${UnicodeIntervalSubIntervalNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        val initial = IntegerInterval.new(IntegerRange.new('a'.toInt(), 'c'.toInt()))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(2L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(1, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals('b'.toInt(), result.primitive.rangeAtOrNull(0)?.from,
                "The range[0].from property is incorrect")
        Assertions.assertEquals('c'.toInt(), result.primitive.rangeAtOrNull(0)?.to,
                "The range[0].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test commons`() {
        val text =
                "${UnicodeIntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.Common.operator}abc${UnicodeIntervalSubIntervalNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        val initial = IntegerInterval.new(IntegerRange.new('a'.toInt(), 'b'.toInt()))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(2L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(1, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals('a'.toInt(), result.primitive.rangeAtOrNull(0)?.from,
                "The range[0].from property is incorrect")
        Assertions.assertEquals('b'.toInt(), result.primitive.rangeAtOrNull(0)?.to,
                "The range[0].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test not commons`() {
        val text =
                "${UnicodeIntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.NotCommon.operator}abc${UnicodeIntervalSubIntervalNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        val initial = IntegerInterval.new(IntegerRange.Companion.new('b'.toInt()))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(2L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals('a'.toInt(), result.primitive.rangeAtOrNull(0)?.from,
                "The range[0].from property is incorrect")
        Assertions.assertEquals('a'.toInt(), result.primitive.rangeAtOrNull(0)?.to,
                "The range[0].to property is incorrect")
        Assertions.assertEquals('c'.toInt(), result.primitive.rangeAtOrNull(1)?.from,
                "The range[1].from property is incorrect")
        Assertions.assertEquals('c'.toInt(), result.primitive.rangeAtOrNull(1)?.to,
                "The range[1].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test reverse add`() {
        val text =
                "${UnicodeIntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.Add.operator}${UnicodeIntervalSubIntervalNode.reversedToken}a${UnicodeIntervalSubIntervalNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        val initial = IntegerInterval.new(IntegerRange.new(3))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(IntegerRange.Unicode.to.toLong(), result.primitive.pointCount,
                "The pointCount property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals(0, result.primitive.rangeAtOrNull(0)?.from, "The range[0].from property is incorrect")
        Assertions.assertEquals('a'.toInt() - 1, result.primitive.rangeAtOrNull(0)?.to,
                "The range[0].to property is incorrect")
        Assertions.assertEquals('a'.toInt() + 1, result.primitive.rangeAtOrNull(1)?.from,
                "The range[1].from property is incorrect")
        Assertions.assertEquals(IntegerRange.Unicode.to, result.primitive.rangeAtOrNull(1)?.to,
                "The range[1].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test reverse sub`() {
        val text =
                "${UnicodeIntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.Sub.operator}${UnicodeIntervalSubIntervalNode.reversedToken}a${UnicodeIntervalSubIntervalNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        val initial = IntegerInterval.new(IntegerRange.new('a'.toInt() - 10, 'a'.toInt() + 10))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(1, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(1, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals('a'.toInt(), result.primitive.rangeAtOrNull(0)?.from,
                "The range[0].from property is incorrect")
        Assertions.assertEquals('a'.toInt(), result.primitive.rangeAtOrNull(0)?.to,
                "The range[0].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test reverse common`() {
        val text =
                "${UnicodeIntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.Common.operator}${UnicodeIntervalSubIntervalNode.reversedToken}a${UnicodeIntervalSubIntervalNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        val initial = IntegerInterval.new(IntegerRange.new('a'.toInt() - 10, 'a'.toInt() + 10))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(20, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals('a'.toInt() - 10, result.primitive.rangeAtOrNull(0)?.from,
                "The range[0].from property is incorrect")
        Assertions.assertEquals('a'.toInt() - 1, result.primitive.rangeAtOrNull(0)?.to,
                "The range[0].to property is incorrect")
        Assertions.assertEquals('a'.toInt() + 1, result.primitive.rangeAtOrNull(1)?.from,
                "The range[1].from property is incorrect")
        Assertions.assertEquals('a'.toInt() + 10, result.primitive.rangeAtOrNull(1)?.to,
                "The range[1].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test reverse not common`() {
        val text =
                "${UnicodeIntervalSubIntervalNode.startToken}${IntervalSubIntervalNode.Operator.NotCommon.operator}${UnicodeIntervalSubIntervalNode.reversedToken}a${UnicodeIntervalSubIntervalNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalSubIntervalNode.Companion::parse)

        // Prepare stack.
        val initial = IntegerInterval.new(IntegerRange.new('a'.toInt() - 10, 'a'.toInt() + 10))
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(initial))

        TestUtils.processAndCheckEmpty(analyzer)

        val result =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmInterval ?: throw Error(
                        "The result must be a LxmInterval")
        Assertions.assertEquals(IntegerRange.Unicode.to - 19L, result.primitive.pointCount,
                "The pointCount property is incorrect")
        Assertions.assertEquals(3, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals(0, result.primitive.rangeAtOrNull(0)?.from, "The range[0].from property is incorrect")
        Assertions.assertEquals('a'.toInt() - 11, result.primitive.rangeAtOrNull(0)?.to,
                "The range[0].to property is incorrect")
        Assertions.assertEquals('a'.toInt(), result.primitive.rangeAtOrNull(1)?.from,
                "The range[1].from property is incorrect")
        Assertions.assertEquals('a'.toInt(), result.primitive.rangeAtOrNull(1)?.to,
                "The range[1].to property is incorrect")
        Assertions.assertEquals('a'.toInt() + 11, result.primitive.rangeAtOrNull(2)?.from,
                "The range[2].from property is incorrect")
        Assertions.assertEquals(IntegerRange.Unicode.to, result.primitive.rangeAtOrNull(2)?.to,
                "The range[2].to property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
