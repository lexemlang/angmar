package org.lexem.angmar.analyzer.nodes.descriptive.statements

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.descriptive.statements.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ExpressionStmtAnalyzerTest {
    @Test
    fun `test normal without params and properties`() {
        val expName = "expr"
        val text = "${ExpressionStmtNode.keyword} $expName ${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ExpressionStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val exp = context.getPropertyValue(analyzer.memory, expName)?.dereference(analyzer.memory) as? LxmExpression
                ?: throw Error("The result must be a LxmExpression")

        Assertions.assertEquals(expName, exp.name, "The name property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(expName))
    }

    @Test
    fun `test normal with params and properties`() {
        val expName = "expr"
        val text =
                "${ExpressionStmtNode.keyword} $expName ${PropertyStyleObjectBlockNode.startToken} ${PropertyStyleObjectBlockNode.endToken} ${FunctionParameterListNode.startToken}${FunctionParameterListNode.endToken} ${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ExpressionStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val exp = context.getPropertyValue(analyzer.memory, expName)?.dereference(analyzer.memory) as? LxmExpression
                ?: throw Error("The result must be a LxmExpression")

        Assertions.assertEquals(expName, exp.name, "The name property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(expName))
    }
}
