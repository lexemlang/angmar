package org.lexem.angmar.compiler.functional.statements

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.selective.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class SelectiveStmtCompiledTest {
    @Test
    fun `test simplify to noop`() {
        val varName = "test"
        val block =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 10 ${BlockStmtNode.endToken}"
        val case = "${ConditionalPatternSelectiveStmtNode.ifKeyword} ${LogicNode.falseLiteral} $block"
        val text = "${SelectiveStmtNode.keyword} ${SelectiveStmtNode.startToken} $case ${SelectiveStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = SelectiveStmtNode.Companion::parse)

        // Prepare context and stack.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( varName, LxmInteger.Num10)

        TestUtils.processAndCheckEmpty(analyzer)

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val variable = finalContext.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The variable must be LxmInteger")

        Assertions.assertEquals(10, variable.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }
}
