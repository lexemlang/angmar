package org.lexem.angmar.analyzer.nodes.functional.statements.loops

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.functional.statements.loops.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ConditionalLoopStmtAnalyzerTest {
    @Test
    fun `test while`() {
        val varName = "test"
        val initialValue = 0
        val finalValue = 5
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} $varName ${RelationalExpressionNode.lowerThanOperator} $finalValue ${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test indexed while`() {
        val varName = "test"
        val indexName = "index"
        val initialValue = 0
        val finalValue = 5
        val text =
                "${InfiniteLoopStmtNode.keyword} $indexName ${ConditionalLoopStmtNode.whileKeyword} $indexName ${RelationalExpressionNode.lowerThanOperator} $finalValue ${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test while with else`() {
        val varName = "test"
        val initialValue = 0
        val finalValue = 3
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} ${LogicNode.falseLiteral} ${BlockStmtNode.startToken} ${BlockStmtNode.endToken} ${LoopClausesStmtNode.elseKeyword} ${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $finalValue ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test while with last`() {
        val varName = "test"
        val initialValue = 0
        val finalValue = 5
        val resultValue = 10
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} $varName ${RelationalExpressionNode.lowerThanOperator} $finalValue ${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken} ${LoopClausesStmtNode.lastKeyword} ${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 5 ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test until`() {
        val varName = "test"
        val initialValue = 0
        val finalValue = 5
        val text =
                "${ConditionalLoopStmtNode.untilKeyword} $varName ${RelationalExpressionNode.greaterOrEqualThanOperator} $finalValue ${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test indexed until`() {
        val varName = "test"
        val indexName = "index"
        val initialValue = 0
        val finalValue = 5
        val text =
                "${InfiniteLoopStmtNode.keyword} $indexName ${ConditionalLoopStmtNode.untilKeyword} $indexName ${RelationalExpressionNode.greaterOrEqualThanOperator} $finalValue ${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test until with else`() {
        val varName = "test"
        val initialValue = 0
        val finalValue = 3
        val text =
                "${ConditionalLoopStmtNode.untilKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken} ${BlockStmtNode.endToken} ${LoopClausesStmtNode.elseKeyword} ${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $finalValue ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test until with last`() {
        val varName = "test"
        val initialValue = 0
        val finalValue = 5
        val resultValue = 10
        val text =
                "${ConditionalLoopStmtNode.untilKeyword} $varName ${RelationalExpressionNode.greaterOrEqualThanOperator} $finalValue ${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken} ${LoopClausesStmtNode.lastKeyword} ${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 5 ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test exit control signal without tag`() {
        val thenBody = ControlWithoutExpressionStmtNode.exitKeyword
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken} $thenBody ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal with tag matching`() {
        val tagName = "tag"
        val thenBody = "${ControlWithoutExpressionStmtNode.exitKeyword}${BlockStmtNode.tagPrefix}$tagName"
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $thenBody ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal with tag not matching`() {
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val thenBody = "${ControlWithoutExpressionStmtNode.exitKeyword}${BlockStmtNode.tagPrefix}$tagNameControl"
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $thenBody ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, ControlWithoutExpressionStmtNode.exitKeyword,
                tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal from else`() {
        val tagName = "tag"
        val elseBlock = "${ControlWithoutExpressionStmtNode.exitKeyword}${BlockStmtNode.tagPrefix}$tagName"
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} ${LogicNode.falseLiteral} ${BlockStmtNode.startToken} ${BlockStmtNode.endToken} ${LoopClausesStmtNode.elseKeyword} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $elseBlock ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal from last`() {
        val tagName = "tag"
        val varName = "test"
        val lastBlock = "${ControlWithoutExpressionStmtNode.exitKeyword}${BlockStmtNode.tagPrefix}$tagName"
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} $varName ${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator} ${LogicNode.trueLiteral} ${BlockStmtNode.endToken} ${LoopClausesStmtNode.lastKeyword} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $lastBlock ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmLogic.False)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName, AnalyzerCommons.Identifiers.HiddenContextTag))
    }

    @Test
    fun `test next control signal without tag`() {
        val indexed = "index"
        val varName = "test"
        val initialValue = 0
        val finalValue = 5
        val resultValue = 4
        val ifThenBlock = ControlWithoutExpressionStmtNode.nextKeyword
        val ifConditionBlock = "$indexed ${RelationalExpressionNode.equalityOperator} 1"
        val thenBlock =
                "${ConditionalStmtNode.ifKeyword} $ifConditionBlock ${BlockStmtNode.startToken} $ifThenBlock ${BlockStmtNode.endToken} \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1"
        val text =
                "${InfiniteLoopStmtNode.keyword} $indexed ${ConditionalLoopStmtNode.whileKeyword} $indexed ${RelationalExpressionNode.lowerThanOperator} $finalValue ${BlockStmtNode.startToken} $thenBlock ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test next control signal with tag matching`() {
        val tagName = "tag"
        val indexed = "index"
        val varName = "test"
        val initialValue = 0
        val finalValue = 5
        val resultValue = 4
        val ifThenBlock = "${ControlWithoutExpressionStmtNode.nextKeyword}${BlockStmtNode.tagPrefix}$tagName"
        val ifConditionBlock = "$indexed ${RelationalExpressionNode.equalityOperator} 1"
        val thenBlock =
                "${ConditionalStmtNode.ifKeyword} $ifConditionBlock ${BlockStmtNode.startToken} $ifThenBlock ${BlockStmtNode.endToken} \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1"
        val text =
                "${InfiniteLoopStmtNode.keyword} $indexed ${ConditionalLoopStmtNode.whileKeyword} $indexed ${RelationalExpressionNode.lowerThanOperator} $finalValue ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $thenBlock ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test next control signal with tag not matching`() {
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val thenBlock = "${ControlWithoutExpressionStmtNode.nextKeyword}${BlockStmtNode.tagPrefix}$tagNameControl"
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $thenBlock ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, ControlWithoutExpressionStmtNode.nextKeyword,
                tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test redo control signal without tag`() {
        val varName = "test"
        val initialValue = 0
        val resultValue = 2
        val ifThenBlock = ControlWithoutExpressionStmtNode.redoKeyword
        val ifConditionBlock = "$varName ${RelationalExpressionNode.equalityOperator} 1"
        val thenBlock =
                "${ConditionalStmtNode.ifKeyword} $ifConditionBlock ${BlockStmtNode.startToken} $ifThenBlock ${BlockStmtNode.endToken}"
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $thenBlock \n ${ControlWithoutExpressionStmtNode.exitKeyword} ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test redo control signal with tag`() {
        val tagName = "tag"
        val varName = "test"
        val initialValue = 0
        val resultValue = 2
        val ifThenBlock = "${ControlWithoutExpressionStmtNode.redoKeyword}${BlockStmtNode.tagPrefix}$tagName"
        val ifConditionBlock = "$varName ${RelationalExpressionNode.equalityOperator} 1"
        val thenBlock =
                "${ConditionalStmtNode.ifKeyword} $ifConditionBlock ${BlockStmtNode.startToken} $ifThenBlock ${BlockStmtNode.endToken}"
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $thenBlock \n ${ControlWithoutExpressionStmtNode.exitKeyword} ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test redo control signal with tag not matching`() {
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val thenBlock = "${ControlWithoutExpressionStmtNode.redoKeyword}${BlockStmtNode.tagPrefix}$tagNameControl"
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $thenBlock ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, ControlWithoutExpressionStmtNode.redoKeyword,
                tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test restart control signal without tag`() {
        val varName = "test"
        val initialValue = 0
        val resultValue = 2
        val ifThenBlock = ControlWithoutExpressionStmtNode.restartKeyword
        val ifConditionBlock = "$varName ${RelationalExpressionNode.equalityOperator} 1"
        val thenBlock =
                "${ConditionalStmtNode.ifKeyword} $ifConditionBlock ${BlockStmtNode.startToken} $ifThenBlock ${BlockStmtNode.endToken}"
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $thenBlock \n ${ControlWithoutExpressionStmtNode.exitKeyword} ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test restart control signal with tag`() {
        val tagName = "tag"
        val varName = "test"
        val initialValue = 0
        val resultValue = 2
        val ifThenBlock = "${ControlWithoutExpressionStmtNode.restartKeyword}${BlockStmtNode.tagPrefix}$tagName"
        val ifConditionBlock = "$varName ${RelationalExpressionNode.equalityOperator} 1"
        val thenBlock =
                "${ConditionalStmtNode.ifKeyword} $ifConditionBlock ${BlockStmtNode.startToken} $ifThenBlock ${BlockStmtNode.endToken}"
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $thenBlock \n ${ControlWithoutExpressionStmtNode.exitKeyword} ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test restart control signal with tag not matching`() {
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val thenBlock = "${ControlWithoutExpressionStmtNode.restartKeyword}${BlockStmtNode.tagPrefix}$tagNameControl"
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $thenBlock ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, ControlWithoutExpressionStmtNode.restartKeyword,
                tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test control signals with expression`(keyword: String) {
        val value = LxmInteger.Num10
        val controlStmt = "$keyword $value"
        val text =
                "${ConditionalLoopStmtNode.whileKeyword} ${LogicNode.trueLiteral} ${BlockStmtNode.startToken} $controlStmt ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ConditionalLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, value) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }
}
