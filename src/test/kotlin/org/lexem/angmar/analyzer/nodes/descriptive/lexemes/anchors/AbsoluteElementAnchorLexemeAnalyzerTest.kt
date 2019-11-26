package org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class AbsoluteElementAnchorLexemeAnalyzerTest {

    companion object {
        @JvmStatic
        private fun provideOptions(): Stream<Arguments> {
            val sequence = sequence {
                for (isOk in listOf(false, true)) {
                    yield(Arguments.of(isOk))
                }
            }

            return sequence.asStream()
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideOptions")
    fun `test from start`(isOk: Boolean) {
        val text = "this is a test"
        val index = 3
        val grammar =
                "${AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromStart.identifier}${IndexerNode.startToken}$index${IndexerNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(grammar, parserFunction = AbsoluteElementAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        val initialPosition = if (isOk) {
            index
        } else {
            0
        }
        textReader.setPosition(initialPosition)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        if (isOk) {
            Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        } else {
            Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        }

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @MethodSource("provideOptions")
    fun `test from end`(isOk: Boolean) {
        val text = "this is a test"
        val index = 3
        val grammar =
                "${AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromEnd.identifier}${IndexerNode.startToken}$index${IndexerNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(grammar, parserFunction = AbsoluteElementAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        val initialPosition = if (isOk) {
            text.length - index
        } else {
            0
        }
        textReader.setPosition(initialPosition)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        if (isOk) {
            Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        } else {
            Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        }

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @MethodSource("provideOptions")
    fun `test from analysis beginning`(isOk: Boolean) {
        val text = "this is a test"
        val index = if (isOk) {
            0
        } else {
            3
        }
        val grammar =
                "${AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.AnalysisBeginning.identifier}${IndexerNode.startToken}$index${IndexerNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(grammar, parserFunction = AbsoluteElementAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        val initialPosition = 5
        textReader.setPosition(initialPosition)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        if (isOk) {
            Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        } else {
            Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        }

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    @Incorrect
    fun `test with no integer result`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType) {
            val text = "this is a test"
            val index = LxmLogic.True
            val grammar =
                    "${AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromStart.identifier}${IndexerNode.startToken}$index${IndexerNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar,
                    parserFunction = AbsoluteElementAnchorLexemeNode.Companion::parse)
            val textReader = IOStringReader.from(text)

            TestUtils.processAndCheckEmpty(analyzer, textReader)
        }
    }
}
