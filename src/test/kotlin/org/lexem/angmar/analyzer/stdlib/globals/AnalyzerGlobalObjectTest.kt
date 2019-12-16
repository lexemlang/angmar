package org.lexem.angmar.analyzer.stdlib.globals

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class AnalyzerGlobalObjectTest {
    @Test
    fun `test rootNode`() {
        val grammar =
                "${AnalyzerGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${AnalyzerGlobalObject.RootNode}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            val result = result?.dereference(analyzer.memory, toWrite = false) as? LxmNode ?: throw Error(
                    "The result must be a LxmNode")
            Assertions.assertEquals(AnalyzerCommons.Identifiers.Root, result.name, "The result is incorrect")
        }
    }
}
