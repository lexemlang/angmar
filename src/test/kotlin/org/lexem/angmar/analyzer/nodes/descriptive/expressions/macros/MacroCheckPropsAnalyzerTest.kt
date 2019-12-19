package org.lexem.angmar.analyzer.nodes.descriptive.expressions.macros

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.descriptive.expressions.macros.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class MacroCheckPropsAnalyzerTest {
    @Test
    fun `test all correct`() {
        val affirmativeVar = "a"
        val negativeVar = "b"
        val withValueVar = "c"
        val withValueValue = 6
        val affirmative = affirmativeVar
        val negative = "${PropertyStyleObjectBlockNode.negativeToken}$negativeVar"
        val withValue =
                "${PropertyStyleObjectBlockNode.setToken}$withValueVar${ParenthesisExpressionNode.startToken}$withValueValue${ParenthesisExpressionNode.endToken}"
        val grammar =
                "${MacroCheckPropsNode.macroName}${PropertyStyleObjectBlockNode.startToken}$affirmative $negative $withValue${PropertyStyleObjectBlockNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MacroCheckPropsNode.Companion::parse,
                isDescriptiveCode = true)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", analyzer.text.saveCursor(), null, analyzer.memory)
        val props = node.getProperties(analyzer.memory, toWrite = true)
        props.setProperty(analyzer.memory, affirmativeVar, LxmLogic.True)
        props.setProperty(analyzer.memory, negativeVar, LxmLogic.False)
        props.setProperty(analyzer.memory, withValueVar, LxmInteger.from(withValueValue))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test any incorrect`() {
        val affirmativeVar = "a"
        val negativeVar = "b"
        val withValueVar = "c"
        val withValueValue = 6
        val affirmative = affirmativeVar
        val negative = "${PropertyStyleObjectBlockNode.negativeToken}$negativeVar"
        val withValue =
                "${PropertyStyleObjectBlockNode.setToken}$withValueVar${ParenthesisExpressionNode.startToken}$withValueValue${ParenthesisExpressionNode.endToken}"
        val grammar =
                "${MacroCheckPropsNode.macroName}${PropertyStyleObjectBlockNode.startToken}$affirmative $negative $withValue${PropertyStyleObjectBlockNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MacroCheckPropsNode.Companion::parse,
                isDescriptiveCode = true)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", analyzer.text.saveCursor(), null, analyzer.memory)
        val props = node.getProperties(analyzer.memory, toWrite = true)
        props.setProperty(analyzer.memory, affirmativeVar, LxmNil)
        props.setProperty(analyzer.memory, negativeVar, LxmLogic.False)
        props.setProperty(analyzer.memory, withValueVar, LxmInteger.from(withValueValue))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }
}
