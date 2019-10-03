package org.lexem.angmar.analyzer.nodes.functional.statements.selective

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.statements.selective.*
import org.lexem.angmar.utils.*

internal class ElsePatternSelectiveStmtAnalyzerTest {
    @Test
    fun test() {
        val text = ElsePatternSelectiveStmtNode.elseKeyword
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = ElsePatternSelectiveStmtNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.pushStack(LxmInteger.Num10)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.popStack()

        Assertions.assertEquals(LxmLogic.True, result, "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
