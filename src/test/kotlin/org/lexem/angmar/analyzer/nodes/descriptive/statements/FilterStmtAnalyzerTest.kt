package org.lexem.angmar.analyzer.nodes.descriptive.statements

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.descriptive.statements.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class FilterStmtAnalyzerTest {
    @Test
    fun `test normal without params and properties`() {
        val filterName = "fil"
        val text = "${FilterStmtNode.keyword} $filterName ${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FilterStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val exp = context.getPropertyValue(analyzer.memory, filterName)?.dereference(analyzer.memory) as? LxmFilter
                ?: throw Error("The result must be a LxmFilter")

        Assertions.assertEquals(filterName, exp.name, "The name property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(filterName))
    }

    @Test
    fun `test normal with params and properties`() {
        val filterName = "fil"
        val text =
                "${FilterStmtNode.keyword} $filterName ${PropertyStyleObjectBlockNode.startToken} ${PropertyStyleObjectBlockNode.endToken} ${FunctionParameterListNode.startToken}${FunctionParameterListNode.endToken} ${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FilterStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val exp = context.getPropertyValue(analyzer.memory, filterName)?.dereference(analyzer.memory) as? LxmFilter
                ?: throw Error("The result must be a LxmFilter")

        Assertions.assertEquals(filterName, exp.name, "The name property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(filterName))
    }
}
