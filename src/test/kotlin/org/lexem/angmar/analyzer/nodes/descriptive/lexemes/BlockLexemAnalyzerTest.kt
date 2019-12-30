package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class BlockLexemAnalyzerTest {
    @Test
    fun `test explicit sensible and normal block`() {
        val text = "t"
        val grammar =
                "${IntervalNode.startToken}a${IntervalElementNode.rangeToken}z${IntervalNode.endToken}${LexemPropertyPostfixNode.negativeToken}${LexemPropertyPostfixNode.insensibleProperty}${LexemPropertyPostfixNode.reversedProperty}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BlockLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        val result = analyzer.memory.getLastFromStack()
        Assertions.assertEquals(text, (result as LxmString).primitive, "The returned value is incorrect")
        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test explicit sensible and reversed block`() {
        val text = "t"
        val grammar =
                "${IntervalNode.startToken}a${IntervalElementNode.rangeToken}z${IntervalNode.endToken}${LexemPropertyPostfixNode.reversedProperty}${LexemPropertyPostfixNode.negativeToken}${LexemPropertyPostfixNode.insensibleProperty}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BlockLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        textReader.setPosition(text.length)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)

        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        val result = analyzer.memory.getLastFromStack()
        Assertions.assertEquals(text, (result as LxmString).primitive, "The returned value is incorrect")
        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test explicit insensible and normal block`() {
        val text = "T"
        val grammar =
                "${IntervalNode.startToken}a${IntervalElementNode.rangeToken}z${IntervalNode.endToken}${LexemPropertyPostfixNode.insensibleProperty}${LexemPropertyPostfixNode.negativeToken}${LexemPropertyPostfixNode.reversedProperty}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BlockLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        val result = analyzer.memory.getLastFromStack()
        Assertions.assertEquals(text, (result as LxmString).primitive, "The returned value is incorrect")
        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test explicit insensible and reversed block`() {
        val text = "T"
        val grammar =
                "${IntervalNode.startToken}a${IntervalElementNode.rangeToken}z${IntervalNode.endToken}${LexemPropertyPostfixNode.insensibleProperty}${LexemPropertyPostfixNode.reversedProperty}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BlockLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        textReader.setPosition(text.length)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        val result = analyzer.memory.getLastFromStack()
        Assertions.assertEquals(text, (result as LxmString).primitive, "The returned value is incorrect")
        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test implicit sensible and normal block`() {
        val text = "t"
        val grammar =
                "${IntervalNode.startToken}a${IntervalElementNode.rangeToken}z${IntervalNode.endToken}${LexemPropertyPostfixNode.reversedToken}${LexemPropertyPostfixNode.insensibleProperty}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BlockLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        val props = node.getProperties(analyzer.memory, toWrite = true)
        props.setProperty( AnalyzerCommons.Properties.Insensible, LxmLogic.True)
        props.setProperty( AnalyzerCommons.Properties.Reverse, LxmLogic.False)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        val result = analyzer.memory.getLastFromStack()
        Assertions.assertEquals(text, (result as LxmString).primitive, "The returned value is incorrect")
        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test implicit sensible and reversed block`() {
        val text = "t"
        val grammar =
                "${IntervalNode.startToken}a${IntervalElementNode.rangeToken}z${IntervalNode.endToken}${LexemPropertyPostfixNode.reversedToken}${LexemPropertyPostfixNode.insensibleProperty}${LexemPropertyPostfixNode.reversedProperty}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BlockLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        textReader.setPosition(text.length)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        val props = node.getProperties(analyzer.memory, toWrite = true)
        props.setProperty( AnalyzerCommons.Properties.Insensible, LxmLogic.True)
        props.setProperty( AnalyzerCommons.Properties.Reverse, LxmLogic.False)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        val result = analyzer.memory.getLastFromStack()
        Assertions.assertEquals(text, (result as LxmString).primitive, "The returned value is incorrect")
        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test implicit insensible and normal block`() {
        val text = "T"
        val grammar = "${IntervalNode.startToken}a${IntervalElementNode.rangeToken}z${IntervalNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BlockLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        val props = node.getProperties(analyzer.memory, toWrite = true)
        props.setProperty( AnalyzerCommons.Properties.Insensible, LxmLogic.True)
        props.setProperty( AnalyzerCommons.Properties.Reverse, LxmLogic.False)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        val result = analyzer.memory.getLastFromStack()
        Assertions.assertEquals(text, (result as LxmString).primitive, "The returned value is incorrect")
        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test implicit insensible and reversed block`() {
        val text = "T"
        val grammar =
                "${IntervalNode.startToken}a${IntervalElementNode.rangeToken}z${IntervalNode.endToken}${LexemPropertyPostfixNode.reversedToken}${LexemPropertyPostfixNode.reversedProperty}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BlockLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)
        textReader.setPosition(text.length)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        val props = node.getProperties(analyzer.memory, toWrite = true)
        props.setProperty( AnalyzerCommons.Properties.Insensible, LxmLogic.True)
        props.setProperty( AnalyzerCommons.Properties.Reverse, LxmLogic.False)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        val result = analyzer.memory.getLastFromStack()
        Assertions.assertEquals(text, (result as LxmString).primitive, "The returned value is incorrect")
        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test not matching`() {
        val text = "T"
        val grammar = "${IntervalNode.startToken}a${IntervalElementNode.rangeToken}z${IntervalNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BlockLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test negative matching`() {
        val text = "T"
        val grammar =
                "${BlockLexemeNode.notOperator}${IntervalNode.startToken}a${IntervalElementNode.rangeToken}z${IntervalNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BlockLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test negative not matching`() {
        val text = "T"
        val grammar =
                "${BlockLexemeNode.notOperator}${IntervalNode.startToken}x${IntervalElementNode.rangeToken}z${IntervalNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BlockLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        val props = node.getProperties(analyzer.memory, toWrite = true)
        props.setProperty( AnalyzerCommons.Properties.Insensible, LxmLogic.True)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }
}
