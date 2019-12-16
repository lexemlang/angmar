package org.lexem.angmar.analyzer.nodes.functional.expressions

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.utils.*

internal class AssignExpressionAnalyzerTest {
    @Test
    fun `test correct`() {
        val varName = "test"
        val text = "$varName ${AssignOperatorNode.assignOperator} 3"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(3, result.primitive, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val property =
                context.getDereferencedProperty<LxmInteger>(analyzer.memory, varName, toWrite = false) ?: throw Error(
                        "The property must be a LxmInteger")

        Assertions.assertEquals(3, property.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    @Incorrect
    fun `test incorrect`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.AssignToConstant) {
            val text = "5 ${AssignOperatorNode.assignOperator} 3"
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
