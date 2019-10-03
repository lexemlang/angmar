package org.lexem.angmar.analyzer.nodes.functional.expressions.binary

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class RelationalExpressionAnalyzerTest {
    @Test
    fun `test equality`() {
        val text = "${LogicNode.trueLiteral} ${RelationalExpressionNode.equalityOperator} ${LogicNode.trueLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.popStack(),
                "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test inequality`() {
        val text = "${LogicNode.trueLiteral} ${RelationalExpressionNode.inequalityOperator} ${LogicNode.trueLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.popStack(),
                "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test identity`() {
        val text = "${LogicNode.trueLiteral} ${RelationalExpressionNode.identityOperator} ${LogicNode.trueLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.popStack(),
                "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test not identity`() {
        val text = "${LogicNode.trueLiteral} ${RelationalExpressionNode.notIdentityOperator} ${LogicNode.trueLiteral}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.popStack(),
                "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test lower than`() {
        val text = "2 ${RelationalExpressionNode.lowerThanOperator} 55"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.popStack(),
                "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test greater than`() {
        val text = "2 ${RelationalExpressionNode.greaterThanOperator} 55"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.popStack(),
                "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test lower or equal than`() {
        val text = "6 ${RelationalExpressionNode.lowerOrEqualThanOperator} 6"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.popStack(),
                "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test greater or equal than`() {
        val text = "6 ${RelationalExpressionNode.greaterOrEqualThanOperator} 6"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.popStack(),
                "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test correct short-circuit mode`() {
        val text =
                "6 ${RelationalExpressionNode.lowerThanOperator} 8 ${RelationalExpressionNode.greaterOrEqualThanOperator} 3"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.popStack(),
                "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test incorrect short-circuit mode`() {
        val text =
                "6 ${RelationalExpressionNode.greaterThanOperator} 8 ${RelationalExpressionNode.greaterOrEqualThanOperator} test${AccessExplicitMemberNode.accessToken}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = RelationalExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.popStack(),
                "The value inserted in the stack is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
