package org.lexem.angmar.analyzer.nodes.functional.expressions.macros

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class MacroBacktrackAnalyzerTest {
    @Test
    fun `test backtracking macro without the data`() {
        val text = MacroBacktrackNode.macroName
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = MacroBacktrackNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer, status = LexemAnalyzer.ProcessStatus.Backward, bigNodeCount = 0)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test backtracking macro with arguments`() {
        val positional = LxmInteger.Num10
        val namedKey = "xx"
        val namedValue = LxmLogic.True
        val text =
                "${MacroBacktrackNode.macroName}${FunctionCallNode.startToken}$positional${FunctionCallNode.argumentSeparator}$namedKey${FunctionCallNamedArgumentNode.relationalToken}$namedValue${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = MacroBacktrackNode.Companion::parse)

        // Prepare the context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty( AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer, status = LexemAnalyzer.ProcessStatus.Backward,
                hasBacktrackingData = true, bigNodeCount = 0)

        val backtrackingData = analyzer.backtrackingData!!

        Assertions.assertEquals(1, backtrackingData.positional.size, "The number of positional arguments is incorrect")
        Assertions.assertEquals(2, backtrackingData.named.size, "The number of named arguments is incorrect")

        Assertions.assertEquals(positional, backtrackingData.positional[0], "The positional argument[0] is incorrect")
        Assertions.assertEquals(namedValue, backtrackingData.named[namedKey],
                "The named argument[$namedKey] is incorrect")
        Assertions.assertEquals(LxmNil, backtrackingData.named[AnalyzerCommons.Identifiers.This],
                "The named argument[${AnalyzerCommons.Identifiers.This}] is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }
}
