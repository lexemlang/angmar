package org.lexem.angmar.analyzer.stdlib.globals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class AnalyzerGlobalObjectTest {
    @Test
    fun `test rootNode`() {
        val varName = "test"
        val value =
                "${AnalyzerGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${AnalyzerGlobalObject.RootNode}"
        val grammar = "$varName ${AssignOperatorNode.assignOperator} $value"
        val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
            LexemFileNode.parse(parser)
        }

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val result = context.getPropertyValue(analyzer.memory, varName)!!.dereference(analyzer.memory) as LxmNode
        Assertions.assertEquals(AnalyzerCommons.Identifiers.Root, result.name, "The result name is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }
}
