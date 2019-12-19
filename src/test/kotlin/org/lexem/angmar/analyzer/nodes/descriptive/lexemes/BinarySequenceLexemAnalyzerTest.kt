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

internal class BinarySequenceLexemAnalyzerTest {
    @Test
    fun `test explicit normal binary sequence`() {
        val binarySequence = byteArrayOf(0xED.toByte(), 0x95.toByte())
        val binarySequenceText = binarySequence.joinToString(" ") { String.format("%02X", it) }
        val grammar =
                "${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}${binarySequenceText}${BitlistNode.endToken}${LexemPropertyPostfixNode.negativeToken}${LexemPropertyPostfixNode.reversedProperty}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BinarySequenceLexemeNode.Companion::parse)
        val binaryReader = IOBinaryReader.from(binarySequence)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", binaryReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, binaryReader)

        val result = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The returned value must be a LxmBitList")
        val (size, content) = binaryReader.readAllContent()
        Assertions.assertEquals(size, result.primitive.size, "The returned value is incorrect")
        Assertions.assertEquals(content, result.primitive.content, "The returned value is incorrect")
        Assertions.assertEquals(size, analyzer.text.currentPosition(), "The lexem has not consumed the bits")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test explicit reversed binary sequence`() {
        val binarySequence = byteArrayOf(0xED.toByte(), 0x95.toByte())
        val binarySequenceReversed = binarySequence.reversed().map { it.reverseBits() }.toByteArray()
        val binarySequenceText = binarySequence.joinToString(" ") { String.format("%02X", it) }
        val grammar =
                "${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}$binarySequenceText${BitlistNode.endToken}${LexemPropertyPostfixNode.reversedProperty}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BinarySequenceLexemeNode.Companion::parse)
        val binaryReader = IOBinaryReader.from(binarySequenceReversed)
        binaryReader.setPosition(binaryReader.getLength())

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", binaryReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, binaryReader)

        val result = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The returned value must be a LxmBitList")
        val (size, content) = IOBinaryReader.from(binarySequence).readAllContent()
        Assertions.assertEquals(size, result.primitive.size, "The returned value is incorrect")
        Assertions.assertEquals(content, result.primitive.content, "The returned value is incorrect")
        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has not consumed the bits")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test implicit normal binary sequence`() {
        val binarySequence = byteArrayOf(0xED.toByte(), 0x95.toByte())
        val binarySequenceText = binarySequence.joinToString(" ") { String.format("%02X", it) }
        val grammar =
                "${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}${binarySequenceText}${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BinarySequenceLexemeNode.Companion::parse)
        val binaryReader = IOBinaryReader.from(binarySequence)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", binaryReader.saveCursor(), null, analyzer.memory)
        val props = node.getProperties(analyzer.memory, toWrite = true)
        props.setProperty(analyzer.memory, AnalyzerCommons.Properties.Reverse, LxmLogic.False)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, binaryReader)

        val result = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The returned value must be a LxmBitList")
        val (size, content) = binaryReader.readAllContent()
        Assertions.assertEquals(size, result.primitive.size, "The returned value is incorrect")
        Assertions.assertEquals(content, result.primitive.content, "The returned value is incorrect")
        Assertions.assertEquals(size, analyzer.text.currentPosition(), "The lexem has not consumed the bits")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test implicit reversed binary sequence`() {
        val binarySequence = byteArrayOf(0xED.toByte(), 0x95.toByte())
        val binarySequenceReversed = binarySequence.reversed().map { it.reverseBits() }.toByteArray()
        val binarySequenceText = binarySequence.joinToString(" ") { String.format("%02X", it) }
        val grammar =
                "${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}$binarySequenceText${BitlistNode.endToken}${LexemPropertyPostfixNode.reversedProperty}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BinarySequenceLexemeNode.Companion::parse)
        val binaryReader = IOBinaryReader.from(binarySequenceReversed)
        binaryReader.setPosition(binaryReader.getLength())

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", binaryReader.saveCursor(), null, analyzer.memory)
        val props = node.getProperties(analyzer.memory, toWrite = true)
        props.setProperty(analyzer.memory, AnalyzerCommons.Properties.Reverse, LxmLogic.False)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, binaryReader)

        val result = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error(
                "The returned value must be a LxmBitList")
        val (size, content) = IOBinaryReader.from(binarySequence).readAllContent()
        Assertions.assertEquals(size, result.primitive.size, "The returned value is incorrect")
        Assertions.assertEquals(content, result.primitive.content, "The returned value is incorrect")
        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has not consumed the bits")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test not matching`() {
        val binarySequence = byteArrayOf(0xED.toByte(), 0x95.toByte())
        val binarySequenceText = binarySequence.joinToString(" ") { String.format("%02X", it) }
        val grammar =
                "${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}${binarySequenceText}${BitlistNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BinarySequenceLexemeNode.Companion::parse)
        val binarySequence2 = byteArrayOf(0xED.toByte(), 0x55.toByte())
        val binaryReader = IOBinaryReader.from(binarySequence2)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", binaryReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, binaryReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test negative matching`() {
        val binarySequence = byteArrayOf(0xED.toByte(), 0x95.toByte())
        val binarySequenceText = binarySequence.joinToString(" ") { String.format("%02X", it) }
        val grammar =
                "${BinarySequenceLexemeNode.notOperator}${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}${binarySequenceText}${BitlistNode.endToken}${LexemPropertyPostfixNode.negativeToken}${LexemPropertyPostfixNode.reversedProperty}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BinarySequenceLexemeNode.Companion::parse)
        val binaryReader = IOBinaryReader.from(binarySequence)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", binaryReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, binaryReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has not consumed the bits")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test negative not matching`() {
        val binarySequence = byteArrayOf(0xED.toByte(), 0x95.toByte())
        val binarySequenceText = binarySequence.joinToString(" ") { String.format("%02X", it) }
        val grammar =
                "${BinarySequenceLexemeNode.notOperator}${BitlistNode.hexadecimalPrefix}${BitlistNode.startToken}${binarySequenceText}${BitlistNode.endToken}${LexemPropertyPostfixNode.negativeToken}${LexemPropertyPostfixNode.reversedProperty}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = BinarySequenceLexemeNode.Companion::parse)
        val binarySequence2 = byteArrayOf(0xfe.toByte(), 0x32.toByte())
        val binaryReader = IOBinaryReader.from(binarySequence2)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", binaryReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, binaryReader)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has consumed some bits")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }
}
