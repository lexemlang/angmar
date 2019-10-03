package org.lexem.angmar.analyzer.nodes.functional.statements.controls

import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.utils.*

internal class ControlWithExpressionStmtAnalyzerTest {
    private val returnedValue = LxmInteger.Num10

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test anonymous statements`(keyword: String) {
        val text = "$keyword $returnedValue"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = StatementCommons::parseAnyControlStatement)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, returnedValue) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
