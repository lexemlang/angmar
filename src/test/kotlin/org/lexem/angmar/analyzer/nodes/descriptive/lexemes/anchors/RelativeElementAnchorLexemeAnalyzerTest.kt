package org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class RelativeElementAnchorLexemeAnalyzerTest {

    companion object {
        @JvmStatic
        private fun provideTextOptions(): Stream<Arguments> {
            val sequence = sequence {
                for (isStart in listOf(false, true)) {
                    for (isForward in listOf(false, true)) {
                        for (isOk in listOf(false, true)) {
                            yield(Arguments.of(isStart, isForward, isOk))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideTextOptionsWithInsensible(): Stream<Arguments> {
            val sequence = sequence {
                for (isStart in listOf(false, true)) {
                    for (isForward in listOf(false, true)) {
                        for (isOk in listOf(false, true)) {
                            for (isInsensible in listOf(false, true)) {
                                if (isInsensible) {
                                    for (isUppercase in listOf(false, true)) {
                                        yield(Arguments.of(isStart, isForward, isOk, isInsensible, isUppercase))
                                    }
                                } else {
                                    for (isUppercase in listOf(false, true)) {
                                        yield(Arguments.of(isStart, isForward, isOk, isInsensible, false))
                                    }
                                }
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
                            for (eol in WhitespaceNode.endOfLineChars) {
                                yield(Arguments.of(isStart, isForward, isOk, eol))
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
    fun `test text`(isStart: Boolean, isForward: Boolean, isOk: Boolean) {
        val text = "this is a test"
        val grammar = RelativeElementAnchorLexemeNode.textIdentifier
        val analyzer =
                TestUtils.createAnalyzerFrom(grammar, parserFunction = RelativeElementAnchorLexemeNode.Companion::parse)
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
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)
        if (!isForward) {
            val props = node.getProperties(analyzer.memory, toWrite = true)
            props.setProperty(analyzer.memory, AnalyzerCommons.Properties.Reverse, LxmLogic.True)
        }

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.AnchorIsStart, LxmLogic.from(isStart))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        if (isOk) {
            Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        } else {
            Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        }

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        // Remove AnchorIsStart and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.AnchorIsStart)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @MethodSource("provideTextOptions")
    fun `test text-like line`(isStart: Boolean, isForward: Boolean, isOk: Boolean) {
        val text = "this is a test"
        val grammar = RelativeElementAnchorLexemeNode.lineIdentifier
        val analyzer =
                TestUtils.createAnalyzerFrom(grammar, parserFunction = RelativeElementAnchorLexemeNode.Companion::parse)
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
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)
        if (!isForward) {
            val props = node.getProperties(analyzer.memory, toWrite = true)
            props.setProperty(analyzer.memory, AnalyzerCommons.Properties.Reverse, LxmLogic.True)
        }

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.AnchorIsStart, LxmLogic.from(isStart))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        if (isOk) {
            Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        } else {
            Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        }

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        // Remove AnchorIsStart and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.AnchorIsStart)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @MethodSource("provideLineOptions")
    fun `test line`(isStart: Boolean, isForward: Boolean, isOk: Boolean, eol: Char) {
        val prefix = "this is"
        val postfix = "a test"
        val text = "$prefix$eol$postfix"
        val grammar = RelativeElementAnchorLexemeNode.lineIdentifier
        val analyzer =
                TestUtils.createAnalyzerFrom(grammar, parserFunction = RelativeElementAnchorLexemeNode.Companion::parse)
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
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)
        if (!isForward) {
            val props = node.getProperties(analyzer.memory, toWrite = true)
            props.setProperty(analyzer.memory, AnalyzerCommons.Properties.Reverse, LxmLogic.True)
        }

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.AnchorIsStart, LxmLogic.from(isStart))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        if (isOk) {
            Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        } else {
            Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        }

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        // Remove AnchorIsStart and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.AnchorIsStart)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @MethodSource("provideTextOptionsWithInsensible")
    fun `test string`(isStart: Boolean, isForward: Boolean, isOk: Boolean, isInsensible: Boolean,
            isUppercase: Boolean) {
        val prefix = "to be or not to be"
        val word = "mIdLe"
        val postfix = "this is the question"
        val text = "$prefix$word$postfix"
        val grammar = "${StringNode.startToken}${if (isInsensible) {
            if (isUppercase) {
                word.toUnicodeUppercase()
            } else {
                word.toUnicodeLowercase()
            }
        } else {
            word
        }}${StringNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(grammar, parserFunction = RelativeElementAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        val initialPosition = if (isOk) {
            if (isForward xor isStart) {
                prefix.length + word.length
            } else {
                prefix.length
            }
        } else {
            prefix.length / 2
        }
        textReader.setPosition(initialPosition)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)
        val props = node.getProperties(analyzer.memory, toWrite = true)
        if (!isForward) {
            props.setProperty(analyzer.memory, AnalyzerCommons.Properties.Reverse, LxmLogic.True)
        }
        if (isInsensible) {
            props.setProperty(analyzer.memory, AnalyzerCommons.Properties.Insensible, LxmLogic.True)
        }

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.AnchorIsStart, LxmLogic.from(isStart))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        if (isOk) {
            Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        } else {
            Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        }

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        // Remove AnchorIsStart and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.AnchorIsStart)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @MethodSource("provideTextOptionsWithInsensible")
    fun `test interval`(isStart: Boolean, isForward: Boolean, isOk: Boolean, isInsensible: Boolean,
            isUppercase: Boolean) {
        val prefix = "to be or not to be"
        val character = "x"
        val postfix = "this is the question"
        val text = "$prefix$character$postfix"
        val grammar = "${UnicodeIntervalAbbrNode.startToken}${if (isInsensible) {
            if (isUppercase) {
                character.toUnicodeUppercase()
            } else {
                character.toUnicodeLowercase()
            }
        } else {
            character
        }}${UnicodeIntervalAbbrNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(grammar, parserFunction = RelativeElementAnchorLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        val initialPosition = if (isOk) {
            if (isForward xor isStart) {
                prefix.length + character.length
            } else {
                prefix.length
            }
        } else {
            prefix.length / 2
        }
        textReader.setPosition(initialPosition)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)
        val props = node.getProperties(analyzer.memory, toWrite = true)
        if (!isForward) {
            props.setProperty(analyzer.memory, AnalyzerCommons.Properties.Reverse, LxmLogic.True)
        }
        if (isInsensible) {
            props.setProperty(analyzer.memory, AnalyzerCommons.Properties.Insensible, LxmLogic.True)
        }

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.AnchorIsStart, LxmLogic.from(isStart))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        if (isOk) {
            Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        } else {
            Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(), "The result value is incorrect")
        }

        Assertions.assertEquals(initialPosition, textReader.currentPosition(), "The anchor has moved the cursor")

        // Remove AnchorIsStart and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.AnchorIsStart)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }
}
