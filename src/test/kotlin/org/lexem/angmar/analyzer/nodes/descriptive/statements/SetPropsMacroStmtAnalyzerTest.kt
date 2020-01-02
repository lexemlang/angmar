package org.lexem.angmar.analyzer.nodes.descriptive.statements

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.descriptive.statements.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class SetPropsMacroStmtAnalyzerTest {
    @Test
    fun test() {
        val affirmativeVar = "a"
        val negativeVar = "b"
        val withValueVar = "c"
        val withValueValue = 6
        val affirmative = affirmativeVar
        val negative = "${PropertyStyleObjectBlockNode.negativeToken}$negativeVar"
        val withValue =
                "${PropertyStyleObjectBlockNode.setToken}$withValueVar${ParenthesisExpressionNode.startToken}$withValueValue${ParenthesisExpressionNode.endToken}"
        val grammar =
                "${SetPropsMacroStmtNode.macroName}${PropertyStyleObjectBlockNode.startToken}$affirmative $negative $withValue${PropertyStyleObjectBlockNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = SetPropsMacroStmtNode.Companion::parse,
                isDescriptiveCode = true)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", analyzer.text.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer)

        val props = AnalyzerCommons.getCurrentNodeProps(analyzer.memory, toWrite = false)
        Assertions.assertEquals(LxmLogic.True, props.getPropertyValue(analyzer.memory, affirmativeVar),
                "The $affirmativeVar property is incorrect")
        Assertions.assertEquals(LxmLogic.False, props.getPropertyValue(analyzer.memory, negativeVar),
                "The $negativeVar property is incorrect")
        Assertions.assertEquals(withValueValue,
                (props.getPropertyValue(analyzer.memory, withValueVar) as? LxmInteger)?.primitive,
                "The $withValueVar property is incorrect")

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }
}
