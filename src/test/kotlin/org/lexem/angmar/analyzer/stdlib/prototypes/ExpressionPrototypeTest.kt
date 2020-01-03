package org.lexem.angmar.analyzer.stdlib.prototypes

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.descriptive.statements.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ExpressionPrototypeTest {
    @Test
    fun `test wrap`() {
        val value = LxmLogic.True
        val expName = "expName"
        val paramName = "param"
        val varName = "varAux"
        val expression =
                "${ExpressionStmtNode.keyword} $expName ${FunctionParameterListNode.startToken}$paramName${FunctionParameterListNode.endToken} ${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $paramName ${BlockStmtNode.endToken}"
        val fnCall = "$expName${AccessExplicitMemberNode.accessToken}${FunctionPrototype.Wrap}"
        val grammar =
                "$fnCall${FunctionCallNode.startToken}$value${FunctionCallNode.endToken}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar, initialVars = mapOf(varName to LxmNil),
                preFunctionCall = expression) { analyzer, result ->
            val resultDeref = result?.dereference(analyzer.memory, toWrite = false) as? LxmNode ?: throw Error(
                    "The result must be a LxmNode")
            Assertions.assertEquals(expName, resultDeref.name, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val result2 = context.getPropertyValue(analyzer.memory, varName)

            Assertions.assertEquals(value, result2, "The result2 is incorrect")

            // Remove the function cyclic reference.
            val hiddenContext = AnalyzerCommons.getHiddenContext(analyzer.memory, toWrite = true)
            val node = hiddenContext.getDereferencedProperty<LxmNode>(analyzer.memory,
                    AnalyzerCommons.Identifiers.HiddenLastResultNode, toWrite = true)!!
            node.clearChildren(analyzer.memory)
        }
    }
}
