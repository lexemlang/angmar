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

internal class GroupLexemAnalyzerTest {
    @Test
    fun `test no header, 1 option - matching - not capturing`() {
        val text = "this is a test"
        val textLexeme = "${StringNode.startToken}$text${StringNode.endToken}"
        val grammar = "${GroupLexemeNode.startToken}$textLexeme${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result must be a LxmNil")
        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        removeNode(analyzer)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test no header, 3 options - matching - not capturing`() {
        val text = "c"
        val textLexeme3 = "${StringNode.startToken}c${StringNode.endToken}"
        val textLexeme2 = "${StringNode.startToken}b${StringNode.endToken}"
        val textLexeme1 = "${StringNode.startToken}a${StringNode.endToken}"
        val grammar = "${GroupLexemeNode.startToken}${listOf(textLexeme1, textLexeme2, textLexeme3).joinToString(
                GroupLexemeNode.patternToken)}${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result must be a LxmNil")
        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        removeNode(analyzer)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test {0} header, 3 options - matching - not capturing`() {
        val text = "acb"
        val textLexeme3 = "${StringNode.startToken}c${StringNode.endToken}"
        val textLexeme2 = "${StringNode.startToken}b${StringNode.endToken}"
        val textLexeme1 = "${StringNode.startToken}a${StringNode.endToken}"
        val header = "${ExplicitQuantifierLexemeNode.startToken}0${ExplicitQuantifierLexemeNode.endToken}"
        val grammar = "${GroupLexemeNode.startToken}$header${GroupLexemeNode.headerRelationalToken}${listOf(textLexeme1,
                textLexeme2, textLexeme3).joinToString(GroupLexemeNode.patternToken)}${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 1)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result must be a LxmNil")
        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        removeNode(analyzer)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test {2} header, 3 options - matching - not capturing`() {
        val text = "acb"
        val textLexeme3 = "${StringNode.startToken}c${StringNode.endToken}"
        val textLexeme2 = "${StringNode.startToken}b${StringNode.endToken}"
        val textLexeme1 = "${StringNode.startToken}a${StringNode.endToken}"
        val header = "${ExplicitQuantifierLexemeNode.startToken}2${ExplicitQuantifierLexemeNode.endToken}"
        val grammar = "${GroupLexemeNode.startToken}$header${GroupLexemeNode.headerRelationalToken}${listOf(textLexeme1,
                textLexeme2, textLexeme3).joinToString(GroupLexemeNode.patternToken)}${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 3)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result must be a LxmNil")
        Assertions.assertEquals(2, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        removeNode(analyzer)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test {2} header, 3 options - not matching - not capturing`() {
        val text = "acb"
        val textLexeme3 = "${StringNode.startToken}z${StringNode.endToken}"
        val textLexeme2 = "${StringNode.startToken}y${StringNode.endToken}"
        val textLexeme1 = "${StringNode.startToken}x${StringNode.endToken}"
        val header = "${ExplicitQuantifierLexemeNode.startToken}2${ExplicitQuantifierLexemeNode.endToken}"
        val grammar = "${GroupLexemeNode.startToken}$header${GroupLexemeNode.headerRelationalToken}${listOf(textLexeme1,
                textLexeme2, textLexeme3).joinToString(GroupLexemeNode.patternToken)}${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has consumed some characters")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test {1,2}? header, 3 options - matching - capturing`() {
        val text = "acb"
        val textLexeme3 = "${StringNode.startToken}c${StringNode.endToken}"
        val textLexeme2 = "${StringNode.startToken}b${StringNode.endToken}"
        val textLexeme1 = "${StringNode.startToken}a${StringNode.endToken}"
        val header =
                "${ExplicitQuantifierLexemeNode.startToken}1${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken}"
        val grammar = "${GroupLexemeNode.startToken}$header${GroupLexemeNode.headerRelationalToken}${listOf(textLexeme1,
                textLexeme2, textLexeme3).joinToString(GroupLexemeNode.patternToken)}${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 3)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result must be a LxmNil")
        Assertions.assertEquals(2, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        removeNode(analyzer)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test nodeName header, 1 option - matching - capturing`() {
        val text = "this is a test"
        val textLexeme = "${StringNode.startToken}$text${StringNode.endToken}"
        val header = "nodeName"
        val grammar =
                "${GroupLexemeNode.startToken}$header${GroupLexemeNode.headerRelationalToken}$textLexeme${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result must be a LxmNil")
        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        removeNode(analyzer)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test no capture without a name`() {
        val text = "this is a test"
        val textLexeme = "${StringNode.startToken}$text${StringNode.endToken}"
        val header =
                "${PropertyStyleObjectBlockNode.startToken}${AnalyzerCommons.Properties.Capture}${PropertyStyleObjectBlockNode.endToken}"
        val grammar =
                "${GroupLexemeNode.startToken}$header${GroupLexemeNode.headerRelationalToken}$textLexeme${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result must be a LxmNil")
        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        removeNode(analyzer)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test capture with a name`() {
        val text = "this is a test"
        val nodeName = "nodeName"
        val textLexeme = "${StringNode.startToken}$text${StringNode.endToken}"
        val header =
                "$nodeName${PropertyStyleObjectBlockNode.startToken}${AnalyzerCommons.Properties.Capture}${PropertyStyleObjectBlockNode.endToken}"
        val grammar =
                "${GroupLexemeNode.startToken}$header${GroupLexemeNode.headerRelationalToken}$textLexeme${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        val result = analyzer.memory.getLastFromStack().dereference(analyzer.memory, toWrite = false) as? LxmNode
                ?: throw Error("The result must be a LxmNode")

        Assertions.assertEquals(nodeName, result.name, "The name property is incorrect")
        Assertions.assertEquals(0, result.getFrom(analyzer.memory).primitive.position(),
                "The from property is incorrect")
        Assertions.assertEquals(text.length, result.getTo(analyzer.memory)?.primitive?.position(),
                "The to property is incorrect")
        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        removeNode(analyzer)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test no consume`() {
        val text = "this is a test"
        val nodeName = "nodeName"
        val textLexeme = "${StringNode.startToken}$text${StringNode.endToken}"
        val header =
                "$nodeName${PropertyStyleObjectBlockNode.startToken}${AnalyzerCommons.Properties.Capture} ${PropertyStyleObjectBlockNode.negativeToken}${AnalyzerCommons.Properties.Consume}${PropertyStyleObjectBlockNode.endToken}"
        val grammar =
                "${GroupLexemeNode.startToken}$header${GroupLexemeNode.headerRelationalToken}$textLexeme${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        val result = analyzer.memory.getLastFromStack().dereference(analyzer.memory, toWrite = false) as? LxmNode
                ?: throw Error("The result must be a LxmNode")

        Assertions.assertEquals(nodeName, result.name, "The name property is incorrect")
        Assertions.assertEquals(0, result.getFrom(analyzer.memory).primitive.position(),
                "The from property is incorrect")
        Assertions.assertEquals(text.length, result.getTo(analyzer.memory)?.primitive?.position(),
                "The to property is incorrect")
        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        removeNode(analyzer)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test backtracking`() {
        val text = "abx"
        val textLexeme3 = "${StringNode.startToken}x${StringNode.endToken}"
        val textLexeme2 = "${StringNode.startToken}ab${StringNode.endToken}"
        val textLexeme1 = "${StringNode.startToken}a${StringNode.endToken}"
        val group = "${GroupLexemeNode.startToken}${listOf(textLexeme1, textLexeme2).joinToString(
                GroupLexemeNode.patternToken)}${GroupLexemeNode.endToken}"
        val grammar = "${GroupLexemeNode.startToken}$group $textLexeme3${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 3)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        removeNode(analyzer)

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test atomic with backtracking`() {
        val text = "abx"
        val textLexeme3 = "${StringNode.startToken}x${StringNode.endToken}"
        val textLexeme2 = "${StringNode.startToken}ab${StringNode.endToken}"
        val textLexeme1 = "${StringNode.startToken}a${StringNode.endToken}"
        val header =
                "${PropertyStyleObjectBlockNode.startToken}${PropertyStyleObjectBlockNode.negativeToken}${AnalyzerCommons.Properties.Backtrack}${PropertyStyleObjectBlockNode.endToken}"
        val group = "${GroupLexemeNode.startToken}$header${GroupLexemeNode.headerRelationalToken}${listOf(textLexeme1,
                textLexeme2).joinToString(GroupLexemeNode.patternToken)}${GroupLexemeNode.endToken}"
        val grammar = "${GroupLexemeNode.startToken}$group $textLexeme3${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has consumed some characters")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test negation - matching`() {
        val text = "this is a test"
        val textLexeme = "${StringNode.startToken}x${StringNode.endToken}"
        val grammar =
                "${GroupLexemeNode.notOperator}${GroupLexemeNode.startToken}$textLexeme${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has consumed some characters")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test negation - not matching`() {
        val text = "this is a test"
        val textLexeme = "${StringNode.startToken}$text${StringNode.endToken}"
        val grammar =
                "${GroupLexemeNode.notOperator}${GroupLexemeNode.startToken}$textLexeme${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has consumed some characters")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test filter mode !consume`() {
        val nodeName = "nodeName"
        val textLexeme = "${FilterLexemeNode.startToken}$nodeName${FilterLexemeNode.endToken}"
        val header =
                "${PropertyStyleObjectBlockNode.startToken}${PropertyStyleObjectBlockNode.negativeToken}${AnalyzerCommons.Properties.Consume}${PropertyStyleObjectBlockNode.endToken}"
        val grammar =
                "${GroupLexemeNode.startToken}$header${GroupLexemeNode.headerRelationalToken}$textLexeme${GroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = GroupLexemeNode.Companion::parse,
                isFilterCode = true)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val lxmNode = LxmNode(analyzer.memory, "createdNode", analyzer.text.saveCursor())

        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNode, isConstant = true)

        // Prepare the node to filter.
        val parent = LxmNode(analyzer.memory, "processedNode", analyzer.text.saveCursor())
        val childNode = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
        childNode.addToParent(analyzer.memory, parent)

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, parent)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer, bigNodeCount = 2)

        val newPosition = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.FilterNodePosition) as LxmInteger
        val list = analyzer.memory.getLastFromStack().dereference(analyzer.memory, toWrite = false) as LxmList

        Assertions.assertEquals(1, list.size, "The result is incorrect")
        Assertions.assertEquals(childNode.getPrimitive().position,
                (list.getCell(analyzer.memory, 0) as LxmReference).position, "The result[0] is incorrect")
        Assertions.assertEquals(0, newPosition.primitive, "The new position is incorrect")

        // Remove FilterNode, FilterNodePosition and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNode)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNodePosition)
        analyzer.memory.removeLastFromStack()

        // Remove the circular references of the nodes.
        childNode.getPrimitive().dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)!!.setProperty(analyzer.memory,
                AnalyzerCommons.Identifiers.Parent, LxmNil, ignoreConstant = true)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    // AUXILIARY METHODS ------------------------------------------------------

    /**
     * Checks all the results of the header.
     */
    private fun removeNode(analyzer: LexemAnalyzer) {
        val hiddenContext = AnalyzerCommons.getHiddenContext(analyzer.memory, toWrite = false)
        val lxmNode = hiddenContext.getPropertyValue(analyzer.memory,
                AnalyzerCommons.Identifiers.HiddenLastResultNode)!!.dereference(analyzer.memory,
                toWrite = false) as LxmNode
        val children = lxmNode.getChildren(analyzer.memory, toWrite = true)

        for (i in children.size - 1 downTo 0) {
            children.removeCell(analyzer.memory, i, ignoreConstant = true)
        }
    }
}
