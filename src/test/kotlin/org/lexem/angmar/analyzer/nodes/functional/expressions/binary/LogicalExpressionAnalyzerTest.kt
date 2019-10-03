package org.lexem.angmar.analyzer.nodes.functional.expressions.binary

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class LogicalExpressionAnalyzerTest {
    @Test
    fun `test and`() {
        val text = "${LogicNode.falseLiteral} ${LogicalExpressionNode.andOperator} ${LogicNode.trueLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = LogicalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.popStack(),
                "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test or`() {
        val text = "${LogicNode.falseLiteral} ${LogicalExpressionNode.orOperator} ${LogicNode.trueLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = LogicalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.popStack(),
                "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test xor`() {
        val text = "${LogicNode.falseLiteral} ${LogicalExpressionNode.xorOperator} ${LogicNode.trueLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = LogicalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.popStack(),
                "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
