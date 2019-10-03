package org.lexem.angmar.analyzer.nodes.functional.statements.controls

import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.utils.*

internal class ControlWithoutExpressionStmtAnalyzerTest {
    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test anonymous statements`(keyword: String) {
        val text = keyword
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = StatementCommons::parseAnyControlStatement)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test tagged statements`(keyword: String) {
        val tagName = "test"
        val text = "$keyword${GlobalCommons.tagPrefix}$tagName"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = StatementCommons::parseAnyControlStatement)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagName, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
