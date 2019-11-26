package org.lexem.angmar.analyzer.nodes.functional.statements

import org.junit.jupiter.api.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class FunctionalExpressionStmtAnalyzerTest {
    @Test
    fun test() {
        val text = LogicNode.trueLiteral
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = FunctionalExpressionStmtNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
