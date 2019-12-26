package org.lexem.angmar.analyzer.nodes.functional.statements.controls

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ControlWithExpressionStmtAnalyzerTest {
    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test anonymous statements`(keyword: String) {
        val returnedValue = LxmInteger.Num10
        val text = "$keyword $returnedValue"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = StatementCommons::parseAnyControlStatement)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, returnedValue) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test anonymous statements with referenced value`(keyword: String) {
        val returnedValue = "${ListNode.startToken}2${ListNode.endToken}"
        val text = "$keyword $returnedValue"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = StatementCommons::parseAnyControlStatement)

        try {
            TestUtils.processAndCheckEmpty(analyzer)
            throw Exception("This method should throw an AngmarAnalyzerException")
        } catch (e: AngmarAnalyzerException) {
            if (e.type != AngmarAnalyzerExceptionType.TestControlSignalRaised) {
                throw e
            }

            // Check stack.
            val controlValue = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl
            Assertions.assertEquals(keyword, controlValue.type, "The type property is incorrect")
            Assertions.assertEquals(null, controlValue.tag, "The tag property is incorrect")

            val result = controlValue.value?.dereference(analyzer.memory, toWrite = false) as? LxmList ?: throw Error(
                    "The value must be a LxmList")
            Assertions.assertEquals(1, result.size, "The number of values is incorrect")
            Assertions.assertEquals(LxmInteger.Num2, result.getCell(analyzer.memory, 0),
                    "The number of values is incorrect")

            analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
