package org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class RelativeAnchorLexemeAnalyzerTest {

    companion object {
        @JvmStatic
        private fun provideTextOptions(): Stream<Arguments> {
            val sequence = sequence {
                for (isStart in listOf(false, true)) {
                    for (isForward in listOf(false, true)) {
                        for (isNegated in listOf(false, true)) {
                            for (isOk in listOf(false, true)) {
                                yield(Arguments.of(isStart, isForward, isNegated, isOk))
                            }
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideLineOptions(): Stream<Arguments> {
            val sequence = sequence {
                for (isStart in listOf(false, true)) {
                    for (isForward in listOf(false, true)) {
                        for (isOk in listOf(false, true)) {
                            for (isNegated in listOf(false, true)) {
                                for (eol in WhitespaceNode.endOfLineChars) {
                                    yield(Arguments.of(isStart, isForward, isNegated, isOk, eol))
                                }
                            }
                        }
                    }
                }
            }

            return sequence.asStream()
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideTextOptions")
    fun `test text`(isStart: Boolean, isForward: Boolean, isNegated: Boolean, isOk: Boolean) {
        val text = "this is a test"
        val grammar = if (isNegated) {
            RelativeAnchorLexemeNode.notOperator
        } else {
            ""
        } + if (isStart) {
            RelativeAnchorLexemeNode.startTextToken
        } else {
            RelativeAnchorLexemeNode.endTextToken
        }
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = RelativeAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        val initialPosition = if (isOk) {
            if (isStart xor isForward) {
                text.length
            } else {
                0
            }
        } else {
            text.length / 2
        }
        textReader.setPosition(initialPosition)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)
        if (!isForward) {
            val props = node.getProperties(toWrite = true)
            props.setProperty(AnalyzerCommons.Properties.Reverse, LxmLogic.True)
        }

        if (isOk xor isNegated) {
            TestUtils.processAndCheckEmpty(analyzer, textReader)

            Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result is incorrect")

            // Remove Last from the stack.
            analyzer.memory.removeLastFromStack()
        } else {
            TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                    bigNodeCount = 0)
        }

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @MethodSource("provideTextOptions")
    fun `test text-like line`(isStart: Boolean, isForward: Boolean, isNegated: Boolean, isOk: Boolean) {
        val text = "this is a test"
        val grammar = if (isNegated) {
            RelativeAnchorLexemeNode.notOperator
        } else {
            ""
        } + if (isStart) {
            RelativeAnchorLexemeNode.startLineToken
        } else {
            RelativeAnchorLexemeNode.endLineToken
        }
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = RelativeAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        val initialPosition = if (isOk) {
            if (isStart xor isForward) {
                text.length
            } else {
                0
            }
        } else {
            text.length / 2
        }
        textReader.setPosition(initialPosition)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)
        if (!isForward) {
            val props = node.getProperties(toWrite = true)
            props.setProperty(AnalyzerCommons.Properties.Reverse, LxmLogic.True)
        }

        if (isOk xor isNegated) {
            TestUtils.processAndCheckEmpty(analyzer, textReader)

            Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result is incorrect")

            // Remove Last from the stack.
            analyzer.memory.removeLastFromStack()
        } else {
            TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                    bigNodeCount = 0)
        }

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @MethodSource("provideLineOptions")
    fun `test line`(isStart: Boolean, isForward: Boolean, isNegated: Boolean, isOk: Boolean, eol: Char) {
        val prefix = "this is"
        val postfix = "a test"
        val text = "$prefix$eol$postfix"
        val grammar = if (isNegated) {
            RelativeAnchorLexemeNode.notOperator
        } else {
            ""
        } + if (isStart) {
            RelativeAnchorLexemeNode.startLineToken
        } else {
            RelativeAnchorLexemeNode.endLineToken
        }
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = RelativeAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        val initialPosition = if (isOk) {
            if (isForward xor isStart) {
                prefix.length
            } else {
                prefix.length + 1
            }
        } else {
            prefix.length / 2
        }
        textReader.setPosition(initialPosition)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)
        if (!isForward) {
            val props = node.getProperties(toWrite = true)
            props.setProperty(AnalyzerCommons.Properties.Reverse, LxmLogic.True)
        }

        if (isOk xor isNegated) {
            TestUtils.processAndCheckEmpty(analyzer, textReader)

            Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result is incorrect")

            // Remove Last from the stack.
            analyzer.memory.removeLastFromStack()
        } else {
            TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                    bigNodeCount = 0)
        }

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test one match start`() {
        val prefix = "this \n"
        val text = "${prefix}is a test"
        val initialPosition = prefix.length
        val anchor1 = RelativeElementAnchorLexemeNode.textIdentifier
        val anchor2 = RelativeElementAnchorLexemeNode.lineIdentifier
        val grammar =
                "${RelativeAnchorLexemeNode.startLineToken}${RelativeAnchorLexemeNode.groupStartToken} $anchor1 $anchor2 ${RelativeAnchorLexemeNode.groupEndToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = RelativeAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        textReader.setPosition(initialPosition)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result is incorrect")
        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test no one match`() {
        val text = "this is a test"
        val initialPosition = 3
        val anchor1 = RelativeElementAnchorLexemeNode.textIdentifier
        val anchor2 = RelativeElementAnchorLexemeNode.lineIdentifier
        val grammar =
                "${RelativeAnchorLexemeNode.startLineToken}${RelativeAnchorLexemeNode.groupStartToken} $anchor1 $anchor2 ${RelativeAnchorLexemeNode.groupEndToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = RelativeAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        textReader.setPosition(initialPosition)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test negative with one match`() {
        val prefix = "this \n"
        val text = "${prefix}is a test"
        val initialPosition = prefix.length
        val anchor1 = RelativeElementAnchorLexemeNode.textIdentifier
        val anchor2 = RelativeElementAnchorLexemeNode.lineIdentifier
        val grammar =
                "${RelativeAnchorLexemeNode.notOperator}${RelativeAnchorLexemeNode.startLineToken}${RelativeAnchorLexemeNode.groupStartToken} $anchor1 $anchor2 ${RelativeAnchorLexemeNode.groupEndToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = RelativeAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        textReader.setPosition(initialPosition)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test negative with no one match`() {
        val text = "this is a test"
        val initialPosition = 3
        val anchor1 = RelativeElementAnchorLexemeNode.textIdentifier
        val anchor2 = RelativeElementAnchorLexemeNode.lineIdentifier
        val grammar =
                "${RelativeAnchorLexemeNode.notOperator}${RelativeAnchorLexemeNode.startLineToken}${RelativeAnchorLexemeNode.groupStartToken} $anchor1 $anchor2 ${RelativeAnchorLexemeNode.groupEndToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = RelativeAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        textReader.setPosition(initialPosition)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result is incorrect")
        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }
}
