package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class UnicodeIntervalAnalyzerTest {
    @Test
    fun `test normal`() {
        val text =
                "${UnicodeIntervalNode.macroName}${UnicodeIntervalAbbrNode.startToken}a x${UnicodeIntervalAbbrNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = UnicodeIntervalNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack() as? LxmInterval ?: throw Error("The result must be a LxmInterval")
        Assertions.assertEquals(2L, result.primitive.pointCount, "The pointCount property is incorrect")
        Assertions.assertEquals(2, result.primitive.rangeCount, "The rangeCount property is incorrect")
        Assertions.assertEquals('a'.toInt(), result.primitive.rangeAtOrNull(0)?.from,
                "The range[0].from property is incorrect")
        Assertions.assertEquals('a'.toInt(), result.primitive.rangeAtOrNull(0)?.to,
                "The range[0].to property is incorrect")
        Assertions.assertEquals('x'.toInt(), result.primitive.rangeAtOrNull(1)?.from,
                "The range[1].from property is incorrect")
        Assertions.assertEquals('x'.toInt(), result.primitive.rangeAtOrNull(1)?.to,
                "The range[1].to property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
