package org.lexem.angmar.compiler.descriptive.lexemes

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.utils.*

internal class QuantifiedGroupModifierCompiledTest {
    @Test
    @Incorrect
    fun `test quantifier with non integer minimum`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.IncompatibleType) {
            val minimum = LxmLogic.False
            val text = "${QuantifiedGroupModifierNode.startToken}$minimum${QuantifiedGroupModifierNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test quantifier with minimum lower than zero`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.IncorrectQuantifierBounds) {
            val minimum = -1
            val text = "${QuantifiedGroupModifierNode.startToken}$minimum${QuantifiedGroupModifierNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)

            // Prepare the context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            context.setProperty( AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                    LxmString.from("test"))

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test quantifier with non integer maximum`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.IncompatibleType) {
            val minimum = 5
            val maximum = LxmNil
            val text =
                    "${QuantifiedGroupModifierNode.startToken}$minimum${QuantifiedGroupModifierNode.elementSeparator}$maximum${QuantifiedGroupModifierNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test quantifier without minimum and with non integer maximum`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.IncompatibleType) {
            val minimum = 5
            val maximum = LxmNil
            val text =
                    "${QuantifiedGroupModifierNode.startToken}${QuantifiedGroupModifierNode.elementSeparator}$maximum${QuantifiedGroupModifierNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test quantifier with maximum lower than minimum`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.IncorrectQuantifierBounds) {
            val minimum = 5
            val maximum = 1
            val text =
                    "${QuantifiedGroupModifierNode.startToken}$minimum${QuantifiedGroupModifierNode.elementSeparator}$maximum${QuantifiedGroupModifierNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test quantifier without minimum and with maximum lower than 0`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.IncorrectQuantifierBounds) {
            val maximum = -1
            val text =
                    "${QuantifiedGroupModifierNode.startToken}${QuantifiedGroupModifierNode.elementSeparator}$maximum${QuantifiedGroupModifierNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedGroupModifierNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
