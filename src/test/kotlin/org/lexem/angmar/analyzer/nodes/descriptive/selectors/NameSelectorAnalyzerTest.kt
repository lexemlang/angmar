package org.lexem.angmar.analyzer.nodes.descriptive.selectors

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.selectors.*
import org.lexem.angmar.utils.*

internal class NameSelectorAnalyzerTest {
    @Test
    fun `test addition`() {
        val nodeName = "nodeName"
        val grammar = nodeName
        val text = "This is a test"
        val initialPosition = text.length / 2
        val analyzer =
                TestUtils.createAnalyzerFrom(grammar, parserFunction = NameSelectorNode.Companion::parseForAddition)
        val reader = IOStringReader.from(text)
        reader.setPosition(initialPosition)

        TestUtils.processAndCheckEmpty(analyzer, reader)

        val result = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node).dereference(analyzer.memory,
                toWrite = false) as? LxmNode ?: throw Error("The result must be a LxmNode")
        Assertions.assertEquals(nodeName, result.name, "The name property is incorrect")
        Assertions.assertEquals(initialPosition, result.getFrom().primitive.position(),
                "The from property is incorrect")
        Assertions.assertEquals(initialPosition, result.getTo()!!.primitive.position(), "The to property is incorrect")

        // Remove Node from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test filter - affirmative - ok`() {
        val nodeName1 = "nodeName1"
        val nodeName2 = "nodeName2"
        val grammar =
                "${NameSelectorNode.groupStartToken}$nodeName1${NameSelectorNode.elementSeparator}$nodeName2${NameSelectorNode.groupEndToken}"
        val text = "This is a test"
        val initialPosition = text.length / 2
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = NameSelectorNode.Companion::parse)
        val reader = IOStringReader.from(text)
        reader.setPosition(initialPosition)

        // Prepare context.
        val node = LxmNode(analyzer.memory, nodeName2, reader.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, reader)

        val result = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node) as LxmReference
        Assertions.assertEquals(node.getPrimitive().position, result.position, "The node is incorrect")
        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test filter - affirmative - nok`() {
        val nodeName1 = "nodeName1"
        val nodeName2 = "nodeName2"
        val actualNodeName = "actualNodeName"
        val grammar =
                "${NameSelectorNode.groupStartToken}$nodeName1${NameSelectorNode.elementSeparator}$nodeName2${NameSelectorNode.groupEndToken}"
        val text = "This is a test"
        val initialPosition = text.length / 2
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = NameSelectorNode.Companion::parse)
        val reader = IOStringReader.from(text)
        reader.setPosition(initialPosition)

        // Prepare context.
        val node = LxmNode(analyzer.memory, actualNodeName, reader.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, reader)

        val result = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node) as LxmReference
        Assertions.assertEquals(node.getPrimitive().position, result.position, "The node is incorrect")
        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test filter - negative - ok`() {
        val nodeName1 = "nodeName1"
        val nodeName2 = "nodeName2"
        val actualNodeName = "actualNodeName"
        val grammar =
                "${NameSelectorNode.notOperator}${NameSelectorNode.groupStartToken}$nodeName1${NameSelectorNode.elementSeparator}$nodeName2${NameSelectorNode.groupEndToken}"
        val text = "This is a test"
        val initialPosition = text.length / 2
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = NameSelectorNode.Companion::parse)
        val reader = IOStringReader.from(text)
        reader.setPosition(initialPosition)

        // Prepare context.
        val node = LxmNode(analyzer.memory, actualNodeName, reader.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, reader)

        val result = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node) as LxmReference
        Assertions.assertEquals(node.getPrimitive().position, result.position, "The node is incorrect")
        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test filter - negative - nok`() {
        val nodeName1 = "nodeName1"
        val nodeName2 = "nodeName2"
        val grammar =
                "${NameSelectorNode.notOperator}${NameSelectorNode.groupStartToken}$nodeName1${NameSelectorNode.elementSeparator}$nodeName2${NameSelectorNode.groupEndToken}"
        val text = "This is a test"
        val initialPosition = text.length / 2
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = NameSelectorNode.Companion::parse)
        val reader = IOStringReader.from(text)
        reader.setPosition(initialPosition)

        // Prepare context.
        val node = LxmNode(analyzer.memory, nodeName2, reader.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, reader)

        val result = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node) as LxmReference
        Assertions.assertEquals(node.getPrimitive().position, result.position, "The node is incorrect")
        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
