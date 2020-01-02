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
import org.lexem.angmar.utils.*

internal class InfiniteLoopStmtAnalyzerTest {
    @Test
    fun `test normal`() {
        val varName = "test"
        val initialValue = 0
        val finalValue = 5
        val loopBody =
                "${ConditionalStmtNode.ifKeyword} $varName ${RelationalExpressionNode.greaterOrEqualThanOperator} $finalValue ${BlockStmtNode.startToken} ${ControlWithoutExpressionStmtNode.exitKeyword} ${BlockStmtNode.endToken}"
        val text =
                "${InfiniteLoopStmtNode.keyword} ${BlockStmtNode.startToken} $loopBody \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test indexed`() {
        val index = "index"
        val varName = "test"
        val initialValue = 0
        val finalValue = 5
        val resultValue = 10
        val loopBody =
                "${ConditionalStmtNode.ifKeyword} $index ${RelationalExpressionNode.greaterOrEqualThanOperator} $finalValue ${BlockStmtNode.startToken} ${ControlWithoutExpressionStmtNode.exitKeyword} ${BlockStmtNode.endToken}"
        val text =
                "${InfiniteLoopStmtNode.keyword} $index ${BlockStmtNode.startToken} $loopBody \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} $index ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test exit control signal without tag`() {
        val thenBody = ControlWithoutExpressionStmtNode.exitKeyword
        val text = "${InfiniteLoopStmtNode.keyword} ${BlockStmtNode.startToken} $thenBody ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal with tag matching`() {
        val tagName = "tag"
        val thenBody = "${ControlWithoutExpressionStmtNode.exitKeyword}${BlockStmtNode.tagPrefix}$tagName"
        val text =
                "${InfiniteLoopStmtNode.keyword} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $thenBody ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal with tag not matching`() {
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val thenBody = "${ControlWithoutExpressionStmtNode.exitKeyword}${BlockStmtNode.tagPrefix}$tagNameControl"
        val text =
                "${InfiniteLoopStmtNode.keyword} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $thenBody ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, ControlWithoutExpressionStmtNode.exitKeyword,
                tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test next control signal without tag`() {
        val index = "index"
        val varName = "test"
        val initialValue = 0
        val finalValue = 5
        val resultValue = 4
        val exitCondition =
                "${ConditionalStmtNode.ifKeyword} $index ${RelationalExpressionNode.greaterOrEqualThanOperator} $finalValue ${BlockStmtNode.startToken} ${ControlWithoutExpressionStmtNode.exitKeyword} ${BlockStmtNode.endToken}"
        val nextCondition =
                "${ConditionalStmtNode.ifKeyword} $index ${RelationalExpressionNode.equalityOperator} 2 ${BlockStmtNode.startToken} ${ControlWithoutExpressionStmtNode.nextKeyword} ${BlockStmtNode.endToken}"
        val text =
                "${InfiniteLoopStmtNode.keyword} $index ${BlockStmtNode.startToken} $nextCondition \n $exitCondition \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test next control signal with tag matching`() {
        val tagName = "tag"
        val index = "index"
        val varName = "test"
        val initialValue = 0
        val finalValue = 5
        val resultValue = 4
        val exitCondition =
                "${ConditionalStmtNode.ifKeyword} $index ${RelationalExpressionNode.greaterOrEqualThanOperator} $finalValue ${BlockStmtNode.startToken} ${ControlWithoutExpressionStmtNode.exitKeyword} ${BlockStmtNode.endToken}"
        val nextCondition =
                "${ConditionalStmtNode.ifKeyword} $index ${RelationalExpressionNode.equalityOperator} 2 ${BlockStmtNode.startToken} ${ControlWithoutExpressionStmtNode.nextKeyword}${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val text =
                "${InfiniteLoopStmtNode.keyword} $index ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $nextCondition \n $exitCondition \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test next control signal with tag not matching`() {
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val text =
                "${InfiniteLoopStmtNode.keyword} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock ${ControlWithoutExpressionStmtNode.nextKeyword}${BlockStmtNode.tagPrefix}$tagNameControl ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, ControlWithoutExpressionStmtNode.nextKeyword,
                tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test redo control signal without tag`() {
        val index = "index"
        val varName = "test"
        val initialValue = 0
        val finalValue = 5
        val resultValue = 6
        val exitCondition =
                "${ConditionalStmtNode.ifKeyword} $index ${RelationalExpressionNode.greaterOrEqualThanOperator} $finalValue ${BlockStmtNode.startToken} ${ControlWithoutExpressionStmtNode.exitKeyword} ${BlockStmtNode.endToken}"
        val nextCondition =
                "${ConditionalStmtNode.ifKeyword} $varName ${RelationalExpressionNode.equalityOperator} 2 ${BlockStmtNode.startToken} ${ControlWithoutExpressionStmtNode.redoKeyword} ${BlockStmtNode.endToken}"
        val text =
                "${InfiniteLoopStmtNode.keyword} $index ${BlockStmtNode.startToken} $exitCondition \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $nextCondition ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test redo control signal with tag matching`() {
        val tagName = "tag"
        val index = "index"
        val varName = "test"
        val initialValue = 0
        val finalValue = 5
        val resultValue = 6
        val exitCondition =
                "${ConditionalStmtNode.ifKeyword} $index ${RelationalExpressionNode.greaterOrEqualThanOperator} $finalValue ${BlockStmtNode.startToken} ${ControlWithoutExpressionStmtNode.exitKeyword} ${BlockStmtNode.endToken}"
        val nextCondition =
                "${ConditionalStmtNode.ifKeyword} $varName ${RelationalExpressionNode.equalityOperator} 2 ${BlockStmtNode.startToken} ${ControlWithoutExpressionStmtNode.redoKeyword}${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val text =
                "${InfiniteLoopStmtNode.keyword} $index ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $exitCondition \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $nextCondition ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test redo control signal with tag not matching`() {
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val text =
                "${InfiniteLoopStmtNode.keyword} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock ${ControlWithoutExpressionStmtNode.redoKeyword}${BlockStmtNode.tagPrefix}$tagNameControl ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, ControlWithoutExpressionStmtNode.redoKeyword,
                tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test restart control signal without tag`() {
        val index = "index"
        val varName = "test"
        val initialValue = 0
        val finalValue = 5
        val resultValue = 7
        val exitCondition =
                "${ConditionalStmtNode.ifKeyword} $index ${RelationalExpressionNode.greaterOrEqualThanOperator} $finalValue ${BlockStmtNode.startToken} ${ControlWithoutExpressionStmtNode.exitKeyword} ${BlockStmtNode.endToken}"
        val nextCondition =
                "${ConditionalStmtNode.ifKeyword} $varName ${RelationalExpressionNode.equalityOperator} 2 ${BlockStmtNode.startToken} ${ControlWithoutExpressionStmtNode.restartKeyword} ${BlockStmtNode.endToken}"
        val text =
                "${InfiniteLoopStmtNode.keyword} $index ${BlockStmtNode.startToken} $exitCondition \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $nextCondition ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test restart control signal with tag matching`() {
        val tagName = "tag"
        val index = "index"
        val varName = "test"
        val initialValue = 0
        val finalValue = 5
        val resultValue = 7
        val exitCondition =
                "${ConditionalStmtNode.ifKeyword} $index ${RelationalExpressionNode.greaterOrEqualThanOperator} $finalValue ${BlockStmtNode.startToken} ${ControlWithoutExpressionStmtNode.exitKeyword} ${BlockStmtNode.endToken}"
        val nextCondition =
                "${ConditionalStmtNode.ifKeyword} $varName ${RelationalExpressionNode.equalityOperator} 2 ${BlockStmtNode.startToken} ${ControlWithoutExpressionStmtNode.restartKeyword}${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val text =
                "${InfiniteLoopStmtNode.keyword} $index ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $exitCondition \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $nextCondition ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(resultValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test restart control signal with tag not matching`() {
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val text =
                "${InfiniteLoopStmtNode.keyword} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock ${ControlWithoutExpressionStmtNode.restartKeyword}${BlockStmtNode.tagPrefix}$tagNameControl ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, ControlWithoutExpressionStmtNode.restartKeyword,
                tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test control signals with expression`(keyword: String) {
        val value = LxmInteger.Num10
        val controlStmt = "$keyword $value"
        val text = "${InfiniteLoopStmtNode.keyword} ${BlockStmtNode.startToken} $controlStmt ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = InfiniteLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, value) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
