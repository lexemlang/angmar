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

        // Prepare context for text lexemes.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val node = LxmNode("name", analyzer.text.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))

        TestUtils.processAndCheckEmpty(analyzer)

        val props = AnalyzerCommons.getCurrentNodeProps(analyzer.memory)
        Assertions.assertEquals(LxmLogic.True, props.getPropertyValue(analyzer.memory, affirmativeVar),
                "The $affirmativeVar property is incorrect")
        Assertions.assertEquals(LxmLogic.False, props.getPropertyValue(analyzer.memory, negativeVar),
                "The $negativeVar property is incorrect")
        Assertions.assertEquals(withValueValue,
                (props.getPropertyValue(analyzer.memory, withValueVar) as? LxmInteger)?.primitive,
                "The $withValueVar property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }
}
