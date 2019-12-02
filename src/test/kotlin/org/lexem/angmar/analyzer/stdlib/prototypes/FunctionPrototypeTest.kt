package org.lexem.angmar.analyzer.stdlib.prototypes

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class FunctionPrototypeTest {
    @Test
    fun `test wrap`() {
        val value = LxmLogic.True
        val paramName = "param"
        val valueTxt =
                "${FunctionNode.keyword} ${FunctionParameterListNode.startToken}$paramName${FunctionParameterListNode.endToken} ${BlockStmtNode.startToken} ${ControlWithExpressionStmtNode.returnKeyword} $paramName ${BlockStmtNode.endToken}"
        val fnCall =
                "${ParenthesisExpressionNode.startToken}$valueTxt${ParenthesisExpressionNode.endToken}${AccessExplicitMemberNode.accessToken}${FunctionPrototype.Wrap}"
        val grammar =
                "$fnCall${FunctionCallNode.startToken}$value${FunctionCallNode.endToken}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            Assertions.assertEquals(value, result, "The result is incorrect")
        }
    }
}
