package org.lexem.angmar.analyzer.nodes.descriptive.selectors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.selectors.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class PropertyAbbreviationSelectorAnalyzerTest {
    @Test
    fun `test addition - affirmative`() {
        val propName = "propName"
        val grammar = propName
        val analyzer = TestUtils.createAnalyzerFrom(grammar,
                parserFunction = PropertyAbbreviationSelectorNode.Companion::parseForAddition)

        // Prepare stack.
        val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
        lxmNode.setTo(analyzer.text.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node).dereference(analyzer.memory,
                toWrite = false) as? LxmNode ?: throw Error("The result must be a LxmNode")
        val props = result.getProperties(toWrite = false)
        Assertions.assertEquals(LxmLogic.True, props.getPropertyValue(propName),
                "The property called $propName is incorrect")

        // Remove Node from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test addition - negative`() {
        val propName = "propName"
        val grammar = "${PropertyAbbreviationSelectorNode.notOperator}$propName"
        val analyzer = TestUtils.createAnalyzerFrom(grammar,
                parserFunction = PropertyAbbreviationSelectorNode.Companion::parseForAddition)

        // Prepare stack.
        val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
        lxmNode.setTo(analyzer.text.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node).dereference(analyzer.memory,
                toWrite = false) as? LxmNode ?: throw Error("The result must be a LxmNode")
        val props = result.getProperties(toWrite = false)
        Assertions.assertEquals(LxmLogic.False, props.getPropertyValue(propName),
                "The property called $propName is incorrect")

        // Remove Node from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test addition - value`() {
        val value = LxmInteger.Num10
        val propName = "propName"
        val grammar =
                "$propName${PropertyBlockSelectorNode.startToken}${value.primitive}${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar,
                parserFunction = PropertyAbbreviationSelectorNode.Companion::parseForAddition)

        // Prepare stack.
        val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
        lxmNode.setTo(analyzer.text.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node).dereference(analyzer.memory,
                toWrite = false) as? LxmNode ?: throw Error("The result must be a LxmNode")
        val props = result.getProperties(toWrite = false)
        Assertions.assertEquals(value, props.getPropertyValue(propName), "The property called $propName is incorrect")

        // Remove Node from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test filter - exist`(isOk: Boolean) {
        val propName = "propName"
        val propValue = LxmInteger.Num10
        val grammar = propName
        val analyzer = TestUtils.createAnalyzerFrom(grammar,
                parserFunction = PropertyAbbreviationSelectorNode.Companion::parse)

        // Prepare stack.
        val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
        if (isOk) {
            val props = lxmNode.getProperties(toWrite = true)
            props.setProperty(propName, propValue)
        }

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(isOk), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test filter - not exist`(isOk: Boolean) {
        val propName = "propName"
        val propValue = LxmInteger.Num10
        val grammar = "${PropertyAbbreviationSelectorNode.notOperator}$propName"
        val analyzer = TestUtils.createAnalyzerFrom(grammar,
                parserFunction = PropertyAbbreviationSelectorNode.Companion::parse)

        // Prepare stack.
        val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
        if (!isOk) {
            val props = lxmNode.getProperties(toWrite = true)
            props.setProperty(propName, propValue)
        }

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(isOk), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test filter - value`(isOk: Boolean) {
        val value = if (isOk) {
            LxmInteger.Num10
        } else {
            LxmNil
        }
        val propName = "propName"
        val grammar = "$propName${PropertyBlockSelectorNode.startToken}$value${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar,
                parserFunction = PropertyAbbreviationSelectorNode.Companion::parse)

        // Prepare stack.
        val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
        val props = lxmNode.getProperties(toWrite = true)
        props.setProperty(propName, LxmNil)

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(isOk), analyzer.memory.getLastFromStack(), "The resultis incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test filter - @name`() {
        val nodeName = "nodeName"
        val propName = AnalyzerCommons.SelectorAtIdentifiers.Name
        val equality =
                "${AnalyzerCommons.Identifiers.DefaultPropertyName}${RelationalExpressionNode.equalityOperator}${StringNode.startToken}$nodeName${StringNode.endToken}"
        val grammar =
                "${PropertyAbbreviationSelectorNode.atPrefix}$propName${PropertyBlockSelectorNode.startToken}$equality${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar,
                parserFunction = PropertyAbbreviationSelectorNode.Companion::parse)

        // Prepare stack.
        val lxmNode = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test filter - @start`() {
        val initialPosition = 3
        val propName = AnalyzerCommons.SelectorAtIdentifiers.Start
        val text = "this is a test"
        val equality =
                "${AnalyzerCommons.Identifiers.DefaultPropertyName}${RelationalExpressionNode.equalityOperator}$initialPosition"
        val grammar =
                "${PropertyAbbreviationSelectorNode.atPrefix}$propName${PropertyBlockSelectorNode.startToken}$equality${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar,
                parserFunction = PropertyAbbreviationSelectorNode.Companion::parse)
        val reader = IOStringReader.from(text)
        reader.setPosition(initialPosition)

        // Prepare stack.
        val lxmNode = LxmNode(analyzer.memory, "nodeName", reader.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

        TestUtils.processAndCheckEmpty(analyzer, reader)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test filter - @end`() {
        val initialPosition = 3
        val propName = AnalyzerCommons.SelectorAtIdentifiers.End
        val text = "this is a test"
        val equality =
                "${AnalyzerCommons.Identifiers.DefaultPropertyName}${RelationalExpressionNode.equalityOperator}$initialPosition"
        val grammar =
                "${PropertyAbbreviationSelectorNode.atPrefix}$propName${PropertyBlockSelectorNode.startToken}$equality${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar,
                parserFunction = PropertyAbbreviationSelectorNode.Companion::parse)
        val reader = IOStringReader.from(text)
        reader.setPosition(initialPosition)

        // Prepare stack.
        val lxmNode = LxmNode(analyzer.memory, "nodeName", reader.saveCursor())
        lxmNode.setTo(reader.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

        TestUtils.processAndCheckEmpty(analyzer, reader)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test filter - @end - null`() {
        val initialPosition = 3
        val propName = AnalyzerCommons.SelectorAtIdentifiers.End
        val text = "this is a test"
        val equality =
                "${AnalyzerCommons.Identifiers.DefaultPropertyName}${RelationalExpressionNode.equalityOperator}$initialPosition"
        val grammar =
                "${PropertyAbbreviationSelectorNode.atPrefix}$propName${PropertyBlockSelectorNode.startToken}$equality${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar,
                parserFunction = PropertyAbbreviationSelectorNode.Companion::parse)
        val reader = IOStringReader.from(text)
        reader.setPosition(initialPosition)

        // Prepare stack.
        val lxmNode = LxmNode(analyzer.memory, "nodeName", reader.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

        TestUtils.processAndCheckEmpty(analyzer, reader)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test filter - @content`() {
        val propName = AnalyzerCommons.SelectorAtIdentifiers.Content
        val prefix = "this "
        val middle = "is a"
        val postfix = " test"
        val text = "$prefix$middle$postfix"
        val equality =
                "${AnalyzerCommons.Identifiers.DefaultPropertyName}${RelationalExpressionNode.equalityOperator}${StringNode.startToken}$middle${StringNode.endToken}"
        val grammar =
                "${PropertyAbbreviationSelectorNode.atPrefix}$propName${PropertyBlockSelectorNode.startToken}$equality${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar,
                parserFunction = PropertyAbbreviationSelectorNode.Companion::parse)
        val reader = IOStringReader.from(text)
        reader.setPosition(prefix.length)

        // Prepare stack.
        val lxmNode = LxmNode(analyzer.memory, "nodeName", reader.saveCursor())
        reader.setPosition(prefix.length + middle.length)
        lxmNode.setTo(reader.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

        TestUtils.processAndCheckEmpty(analyzer, reader)

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test filter - @content - null`() {
        val propName = AnalyzerCommons.SelectorAtIdentifiers.Content
        val prefix = "this "
        val middle = "is a"
        val postfix = " test"
        val text = "$prefix$middle$postfix"
        val equality =
                "${AnalyzerCommons.Identifiers.DefaultPropertyName}${RelationalExpressionNode.equalityOperator}${StringNode.startToken}$middle${StringNode.endToken}"
        val grammar =
                "${PropertyAbbreviationSelectorNode.atPrefix}$propName${PropertyBlockSelectorNode.startToken}$equality${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar,
                parserFunction = PropertyAbbreviationSelectorNode.Companion::parse)
        val reader = IOStringReader.from(text)
        reader.setPosition(prefix.length)

        // Prepare stack.
        val lxmNode = LxmNode(analyzer.memory, "nodeName", reader.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

        TestUtils.processAndCheckEmpty(analyzer, reader)

        Assertions.assertEquals(LxmLogic.False, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
