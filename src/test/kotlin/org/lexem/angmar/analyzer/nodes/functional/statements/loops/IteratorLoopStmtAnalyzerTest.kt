package org.lexem.angmar.analyzer.nodes.functional.statements.loops

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.functional.statements.loops.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class IteratorLoopStmtAnalyzerTest {
    @Test
    fun `test over string`() {
        val varName = "variable"
        val containerName = "container"
        val iteratorElement = "test"
        val initialValue = 0
        val finalValue = iteratorElement.length
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test over interval`() {
        val varName = "variable"
        val containerName = "container"
        val initialValue = 0
        val finalValue = 10
        val iteratorElementTxt =
                "${IntervalNode.macroName}${IntervalNode.startToken}0${IntervalElementNode.rangeToken}9${IntervalNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test over list`() {
        val varName = "variable"
        val containerName = "container"
        val iteratorElement = listOf(1, 2, 3, 4)
        val initialValue = 0
        val finalValue = iteratorElement.reduce { acc, i -> acc + i }
        val iteratorElementTxt =
                "${ListNode.startToken}${iteratorElement.joinToString(ListNode.elementSeparator)}${ListNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} $containerName ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test over set`() {
        val varName = "variable"
        val containerName = "container"
        val iteratorElement = listOf(1, 2, 3, 4)
        val initialValue = 0
        val finalValue = iteratorElement.reduce { acc, i -> acc + i }
        val iteratorElementTxt = "${SetNode.macroName}${ListNode.startToken}${iteratorElement.joinToString(
                ListNode.elementSeparator)}${ListNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} $containerName ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test over map`() {
        val varName = "variable"
        val containerName = "container"
        val iteratorElement = mapOf(5 to 1, 54.4 to 2, true to 3, false to 4)
        val initialValue = 0
        val finalValue = iteratorElement.map { it.value }.reduce { acc, i -> acc + i }
        val iteratorElementKeys = iteratorElement.map { "${it.key}${MapElementNode.keyValueSeparator}${it.value}" }
        val iteratorElementTxt = "${MapNode.macroName}${MapNode.startToken}${iteratorElementKeys.joinToString(
                ListNode.elementSeparator)}${MapNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} $containerName${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.Value} ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test over object`() {
        val varName = "variable"
        val containerName = "container"
        val iteratorElement = mapOf("a" to 1, "b" to 2, "c" to 3, "d" to 4)
        val initialValue = 0
        val finalValue = iteratorElement.map { it.value }.reduce { acc, i -> acc + i }
        val iteratorElementKeys = iteratorElement.map { "${it.key}${ObjectElementNode.keyValueSeparator}${it.value}" }
        val iteratorElementTxt = "${ObjectNode.startToken}${iteratorElementKeys.joinToString(
                ListNode.elementSeparator)}${ObjectNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} $containerName${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.Value} ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test over object with index`() {
        val varName = "variable"
        val varNameIndex = "variable_index"
        val varNameKey = "variable_key"
        val index = "index"
        val containerName = "container"
        val iteratorElement = mapOf("a" to 11, "b" to 12, "c" to 13, "d" to 14)
        val iteratorElementKeys = iteratorElement.map { "${it.key}${ObjectElementNode.keyValueSeparator}${it.value}" }
        val iteratorElementTxt = "${ObjectNode.startToken}${iteratorElementKeys.joinToString(
                ListNode.elementSeparator)}${ObjectNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $containerName${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.Value} \n $varNameIndex ${AssignOperatorNode.assignOperator} $index \n $varNameKey ${AssignOperatorNode.assignOperator} $containerName${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.Key} ${BlockStmtNode.endToken}"
        val text =
                "${InfiniteLoopStmtNode.keyword} $index ${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)
        context.setProperty(analyzer.memory, varNameIndex, LxmNil)
        context.setProperty(analyzer.memory, varNameKey, LxmNil)

        TestUtils.processAndCheckEmpty(analyzer)

        val varNameResult = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)
        val varNameIndexResult = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varNameIndex)
        val varNameKeyResult = AnalyzerCommons.getCurrentContextElement<LxmString>(analyzer.memory, varNameKey)

        Assertions.assertEquals(14, varNameResult.primitive, "The primitive property is incorrect")
        Assertions.assertEquals(iteratorElement.size - 1, varNameIndexResult.primitive,
                "The primitive property is incorrect")
        Assertions.assertEquals("d", varNameKeyResult.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName, varNameIndex, varNameKey))
    }

    @Test
    fun `test destroy is called`() {
        val varName = "variable"
        val containerName = "container"
        val conditionName = "condition"
        val iteratorElement = listOf(1, 2, 3, 4)
        val initialValue = 0
        val finalValue = iteratorElement.reduce { acc, i -> acc + i }
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} $containerName ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $conditionName $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val list = LxmList()
        val listRef = analyzer.memory.add(list)
        listRef.increaseReferenceCount(analyzer.memory)
        for (i in iteratorElement) {
            list.addCell(analyzer.memory, LxmInteger.from(i))
        }
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, conditionName, listRef)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        // Decrease the reference count.
        listRef.decreaseReferenceCount(analyzer.memory)

        val listCell = analyzer.memory.lastNode.getCell(listRef.position)

        Assertions.assertTrue(listCell.isFreed, "The object has not been freed")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName, conditionName))
    }

    @Test
    fun `test empty string with else`() {
        val varName = "variable"
        val containerName = "container"
        val iteratorElement = ""
        val initialValue = 0
        val finalValue = iteratorElement.length
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val elseBody =
                "${LoopClausesStmtNode.elseKeyword} ${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $finalValue ${BlockStmtNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body $elseBody"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test string with last`() {
        val varName = "variable"
        val containerName = "container"
        val iteratorElement = "test"
        val initialValue = 0
        val finalValue = 80
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val lastBody =
                "${LoopClausesStmtNode.lastKeyword} ${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} 80 ${BlockStmtNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body $lastBody"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test exit control signal without tag`() {
        val keyword = ControlWithoutExpressionStmtNode.exitKeyword
        val containerName = "container"
        val iteratorElement = "test"
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val body = "${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal with tag matching`() {
        val keyword = ControlWithoutExpressionStmtNode.exitKeyword
        val tagName = "tag"
        val containerName = "container"
        val iteratorElement = "test"
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $keyword${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal with tag not matching`() {
        val keyword = ControlWithoutExpressionStmtNode.exitKeyword
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val containerName = "container"
        val iteratorElement = "test"
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $keyword${BlockStmtNode.tagPrefix}$tagNameControl ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal from else`() {
        val tagName = "tag"
        val containerName = "container"
        val iteratorElement = "test"
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val elseBody =
                "${LoopClausesStmtNode.elseKeyword} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName ${ControlWithoutExpressionStmtNode.exitKeyword}${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val body = "${BlockStmtNode.startToken} ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body $elseBody"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal from last`() {
        val tagName = "tag"
        val containerName = "container"
        val iteratorElement = "test"
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val lastBody =
                "${LoopClausesStmtNode.lastKeyword} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName ${ControlWithoutExpressionStmtNode.exitKeyword}${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val body = "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}xx ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body $lastBody"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test next control signal without tag`() {
        val keyword = ControlWithoutExpressionStmtNode.nextKeyword
        val varName = "variable"
        val containerName = "container"
        val iteratorElement = "test"
        val initialValue = 0
        val finalValue = 0
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $keyword \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test next control signal with tag matching`() {
        val tagName = "tag"
        val keyword = ControlWithoutExpressionStmtNode.nextKeyword
        val varName = "variable"
        val containerName = "container"
        val iteratorElement = "test"
        val initialValue = 0
        val finalValue = 0
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $keyword${BlockStmtNode.tagPrefix}$tagName \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test next control signal with tag not matching`() {
        val keyword = ControlWithoutExpressionStmtNode.nextKeyword
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val containerName = "container"
        val iteratorElement = "test"
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $keyword${BlockStmtNode.tagPrefix}$tagNameControl ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test redo control signal without tag`() {
        val keyword = ControlWithoutExpressionStmtNode.redoKeyword
        val varName = "variable"
        val containerName = "container"
        val iteratorElement = "test"
        val initialValue = 0
        val finalValue = iteratorElement.length + 1
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val conditional =
                "${ConditionalStmtNode.ifKeyword} $varName ${RelationalExpressionNode.equalityOperator} 2 ${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $conditional ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test redo control signal with tag`() {
        val keyword = ControlWithoutExpressionStmtNode.redoKeyword
        val tagName = "tag"
        val varName = "variable"
        val containerName = "container"
        val iteratorElement = "test"
        val initialValue = 0
        val finalValue = iteratorElement.length + 1
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val conditional =
                "${ConditionalStmtNode.ifKeyword} $varName ${RelationalExpressionNode.equalityOperator} 2 ${BlockStmtNode.startToken} $keyword${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $conditional ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test redo control signal with tag not matching`() {
        val keyword = ControlWithoutExpressionStmtNode.redoKeyword
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val containerName = "container"
        val iteratorElement = "test"
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $keyword${BlockStmtNode.tagPrefix}$tagNameControl ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test restart control signal without tag`() {
        val keyword = ControlWithoutExpressionStmtNode.restartKeyword
        val varName = "variable"
        val containerName = "container"
        val iteratorElement = "test"
        val initialValue = 0
        val finalValue = iteratorElement.length + 2
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val conditional =
                "${ConditionalStmtNode.ifKeyword} $varName ${RelationalExpressionNode.equalityOperator} 2 ${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $conditional ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test restart control signal with tag`() {
        val keyword = ControlWithoutExpressionStmtNode.restartKeyword
        val tagName = "tag"
        val varName = "variable"
        val containerName = "container"
        val iteratorElement = "test"
        val initialValue = 0
        val finalValue = iteratorElement.length + 2
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val conditional =
                "${ConditionalStmtNode.ifKeyword} $varName ${RelationalExpressionNode.equalityOperator} 2 ${BlockStmtNode.startToken} $keyword${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $conditional ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test restart control signal with tag not matching`() {
        val keyword = ControlWithoutExpressionStmtNode.restartKeyword
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val containerName = "container"
        val iteratorElement = "test"
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $keyword${BlockStmtNode.tagPrefix}$tagNameControl ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test control signals with expression`(keyword: String) {
        val value = LxmInteger.Num10
        val containerName = "container"
        val iteratorElement = "test"
        val iteratorElementTxt = "${StringNode.startToken}$iteratorElement${StringNode.endToken}"
        val body = "${BlockStmtNode.startToken} $keyword $value ${BlockStmtNode.endToken}"
        val text =
                "${IteratorLoopStmtNode.keyword} $containerName ${IteratorLoopStmtNode.relationKeyword} $iteratorElementTxt $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = IteratorLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, value) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
