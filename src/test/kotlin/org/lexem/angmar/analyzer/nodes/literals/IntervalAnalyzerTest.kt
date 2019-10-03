package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class IntervalAnalyzerTest {
    @Test
    fun `test normal`() {
        val text = "${IntervalNode.macroName}${IntervalNode.startToken}2 4${IntervalNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmInterval ?: throw Error("The result must be a LxmInterval")
        Assertions.assertEquals(2L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeAtOrNull(0)?.from, "The range[0].from property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeAtOrNull(0)?.to, "The range[0].to property is incorrect")
        Assertions.assertEquals(4, result.primitive.rangeAtOrNull(1)?.from, "The range[1].from property is incorrect")
        Assertions.assertEquals(4, result.primitive.rangeAtOrNull(1)?.to, "The range[1].to property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test reversed`() {
        val text =
                "${IntervalNode.macroName}${IntervalNode.startToken}${IntervalNode.reversedToken}2 4${IntervalNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IntervalNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmInterval ?: throw Error("The result must be a LxmInterval")
        Assertions.assertEquals(Int.MAX_VALUE - 1L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(3, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals(0, result.primitive.rangeAtOrNull(0)?.from, "The range[0].from property is incorrect")
        Assertions.assertEquals(1, result.primitive.rangeAtOrNull(0)?.to, "The range[0].to property is incorrect")
        Assertions.assertEquals(3, result.primitive.rangeAtOrNull(1)?.from, "The range[1].from property is incorrect")
        Assertions.assertEquals(3, result.primitive.rangeAtOrNull(1)?.to, "The range[1].to property is incorrect")
        Assertions.assertEquals(5, result.primitive.rangeAtOrNull(2)?.from, "The range[2].from property is incorrect")
        Assertions.assertEquals(Int.MAX_VALUE, result.primitive.rangeAtOrNull(2)?.to,
                "The range[2].to property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
