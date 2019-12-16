package org.lexem.angmar.analyzer.nodes.descriptive.statements.loops

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*
import org.lexem.angmar.parser.descriptive.statements.loops.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.functional.statements.loops.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class QuantifiedLoopStmtAnalyzerTest {
    @Test
    fun `test over greedy`() {
        val varName = "variable"
        val initialValue = 0
        val finalValue = 5
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val quantifier =
                "${ExplicitQuantifierLexemeNode.startToken}3${ExplicitQuantifierLexemeNode.elementSeparator}5${ExplicitQuantifierLexemeNode.endToken}"
        val text = "${QuantifiedLoopStmtNode.keyword} $quantifier $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer, bigNodeCount = 3)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test over lazy`() {
        val varName = "variable"
        val initialValue = 0
        val finalValue = 3
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val quantifier =
                "${ExplicitQuantifierLexemeNode.startToken}3${ExplicitQuantifierLexemeNode.elementSeparator}5${ExplicitQuantifierLexemeNode.endToken}${QuantifierLexemeNode.lazyAbbreviation}"
        val text = "${QuantifiedLoopStmtNode.keyword} $quantifier $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer, bigNodeCount = 2)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test with index`() {
        val varName = "variable"
        val index = "index"
        val initialValue = 0
        val finalValue = 10
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} $index ${BlockStmtNode.endToken}"
        val quantifier =
                "${ExplicitQuantifierLexemeNode.startToken}3${ExplicitQuantifierLexemeNode.elementSeparator}5${ExplicitQuantifierLexemeNode.endToken}"
        val text = "${InfiniteLoopStmtNode.keyword} $index ${QuantifiedLoopStmtNode.keyword} $quantifier $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer, bigNodeCount = 3)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test atomic`() {
        val text = "a"
        val body =
                "${BlockStmtNode.startToken} ${LexemePatternNode.patternToken}${LexemePatternNode.staticTypeToken} ${StringNode.startToken}a${StringNode.endToken} ${BlockStmtNode.endToken}"
        val quantifier =
                "${ExplicitQuantifierLexemeNode.startToken}0${ExplicitQuantifierLexemeNode.elementSeparator}${ExplicitQuantifierLexemeNode.endToken}${QuantifierLexemeNode.atomicLazyAbbreviations}"
        val grammar =
                "${BlockStmtNode.startToken}${QuantifiedLoopStmtNode.keyword} $quantifier $body \n ${LexemePatternNode.patternToken}${LexemePatternNode.staticTypeToken} ${RelativeAnchorLexemeNode.endTextToken}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, isDescriptiveCode = true,
                parserFunction = BlockStmtNode.Companion::parse)
        val reader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", reader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))

        TestUtils.processAndCheckEmpty(analyzer, reader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(0, reader.currentPosition(), "The analyzer has consumed some characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test with else`() {
        val varName = "variable"
        val initialValue = 0
        val finalValue = 5
        val elseBody =
                "${LoopClausesStmtNode.elseKeyword} ${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $finalValue ${BlockStmtNode.endToken}"
        val body = "${BlockStmtNode.startToken} ${MacroBacktrackNode.macroName} ${BlockStmtNode.endToken}"
        val quantifier =
                "${ExplicitQuantifierLexemeNode.startToken}0${ExplicitQuantifierLexemeNode.elementSeparator}5${ExplicitQuantifierLexemeNode.endToken}"
        val text = "${QuantifiedLoopStmtNode.keyword} $quantifier $body $elseBody"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test with last`() {
        val varName = "variable"
        val initialValue = 0
        val finalValue = 50
        val lastBody =
                "${LoopClausesStmtNode.lastKeyword} ${BlockStmtNode.startToken} $varName ${MultiplicativeExpressionNode.multiplicationOperator}${AssignOperatorNode.assignOperator} 10 ${BlockStmtNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val quantifier =
                "${ExplicitQuantifierLexemeNode.startToken}3${ExplicitQuantifierLexemeNode.elementSeparator}5${ExplicitQuantifierLexemeNode.endToken}"
        val text = "${QuantifiedLoopStmtNode.keyword} $quantifier $body $lastBody"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer, bigNodeCount = 3)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test exit control signal without tag`() {
        val keyword = ControlWithoutExpressionStmtNode.exitKeyword
        val body = "${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
        val text = "${QuantifiedLoopStmtNode.keyword} ${QuantifierLexemeNode.atomicGreedyAbbreviations} $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal with tag matching`() {
        val keyword = ControlWithoutExpressionStmtNode.exitKeyword
        val tagName = "tag"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $keyword${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val text = "${QuantifiedLoopStmtNode.keyword} ${QuantifierLexemeNode.atomicGreedyAbbreviations} $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal with tag not matching`() {
        val keyword = ControlWithoutExpressionStmtNode.exitKeyword
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $keyword${BlockStmtNode.tagPrefix}$tagNameControl ${BlockStmtNode.endToken}"
        val text = "${QuantifiedLoopStmtNode.keyword} ${QuantifierLexemeNode.atomicGreedyAbbreviations} $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal from else`() {
        val tagName = "tag"
        val elseBody =
                "${LoopClausesStmtNode.elseKeyword} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName ${ControlWithoutExpressionStmtNode.exitKeyword}${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val body = "${BlockStmtNode.startToken} ${BlockStmtNode.endToken}"
        val text =
                "${QuantifiedLoopStmtNode.keyword} ${ExplicitQuantifierLexemeNode.startToken}0${ExplicitQuantifierLexemeNode.endToken} $body $elseBody"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test exit control signal from last`() {
        val tagName = "tag"
        val lastBody =
                "${LoopClausesStmtNode.lastKeyword} ${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName ${ControlWithoutExpressionStmtNode.exitKeyword}${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val body = "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}xx ${BlockStmtNode.endToken}"
        val text = "${QuantifiedLoopStmtNode.keyword} ${QuantifierLexemeNode.lazyAbbreviation} $body $lastBody"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer, bigNodeCount = 2)
        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test next control signal without tag`() {
        val keyword = ControlWithoutExpressionStmtNode.nextKeyword
        val varName = "variable"
        val initialValue = 0
        val finalValue = 0
        val body =
                "${BlockStmtNode.startToken} $keyword \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val text =
                "${QuantifiedLoopStmtNode.keyword} ${ExplicitQuantifierLexemeNode.startToken}0${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken} $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer, bigNodeCount = 3)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test next control signal with tag matching`() {
        val tagName = "tag"
        val keyword = ControlWithoutExpressionStmtNode.nextKeyword
        val varName = "variable"
        val initialValue = 0
        val finalValue = 0
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $keyword${BlockStmtNode.tagPrefix}$tagName \n $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 ${BlockStmtNode.endToken}"
        val text =
                "${QuantifiedLoopStmtNode.keyword} ${ExplicitQuantifierLexemeNode.startToken}0${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken} $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))

        TestUtils.processAndCheckEmpty(analyzer, bigNodeCount = 3)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test next control signal with tag not matching`() {
        val keyword = ControlWithoutExpressionStmtNode.nextKeyword
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $keyword${BlockStmtNode.tagPrefix}$tagNameControl ${BlockStmtNode.endToken}"
        val text =
                "${QuantifiedLoopStmtNode.keyword} ${ExplicitQuantifierLexemeNode.startToken}0${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken} $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test redo control signal without tag`() {
        val keyword = ControlWithoutExpressionStmtNode.redoKeyword
        val varName = "variable"
        val initialValue = 0
        val finalValue = 3
        val conditional =
                "${ConditionalStmtNode.ifKeyword} $varName ${RelationalExpressionNode.equalityOperator} 2 ${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $conditional ${BlockStmtNode.endToken}"
        val text =
                "${QuantifiedLoopStmtNode.keyword} ${ExplicitQuantifierLexemeNode.startToken}0${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken} $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer, bigNodeCount = 3)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test redo control signal with tag`() {
        val keyword = ControlWithoutExpressionStmtNode.redoKeyword
        val tagName = "tag"
        val varName = "variable"
        val initialValue = 0
        val finalValue = 3
        val conditional =
                "${ConditionalStmtNode.ifKeyword} $varName ${RelationalExpressionNode.equalityOperator} 2 ${BlockStmtNode.startToken} $keyword${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $conditional ${BlockStmtNode.endToken}"
        val text =
                "${QuantifiedLoopStmtNode.keyword} ${ExplicitQuantifierLexemeNode.startToken}0${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken} $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer, bigNodeCount = 3)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test redo control signal with tag not matching`() {
        val keyword = ControlWithoutExpressionStmtNode.redoKeyword
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $keyword${BlockStmtNode.tagPrefix}$tagNameControl ${BlockStmtNode.endToken}"
        val text =
                "${QuantifiedLoopStmtNode.keyword} ${ExplicitQuantifierLexemeNode.startToken}0${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken} $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test restart control signal without tag`() {
        val keyword = ControlWithoutExpressionStmtNode.restartKeyword
        val varName = "variable"
        val initialValue = 0
        val finalValue = 4
        val conditional =
                "${ConditionalStmtNode.ifKeyword} $varName ${RelationalExpressionNode.equalityOperator} 2 ${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
        val body =
                "${BlockStmtNode.startToken} $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $conditional ${BlockStmtNode.endToken}"
        val text =
                "${QuantifiedLoopStmtNode.keyword} ${ExplicitQuantifierLexemeNode.startToken}0${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken} $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer, bigNodeCount = 5)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test restart control signal with tag`() {
        val keyword = ControlWithoutExpressionStmtNode.restartKeyword
        val tagName = "tag"
        val varName = "variable"
        val initialValue = 0
        val finalValue = 4
        val conditional =
                "${ConditionalStmtNode.ifKeyword} $varName ${RelationalExpressionNode.equalityOperator} 2 ${BlockStmtNode.startToken} $keyword${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1 \n $conditional ${BlockStmtNode.endToken}"
        val text =
                "${QuantifiedLoopStmtNode.keyword} ${ExplicitQuantifierLexemeNode.startToken}0${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken} $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmInteger.from(initialValue))
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer, bigNodeCount = 5)

        val result = AnalyzerCommons.getCurrentContextElement<LxmInteger>(analyzer.memory, varName, toWrite = false)

        Assertions.assertEquals(finalValue, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test restart control signal with tag not matching`() {
        val keyword = ControlWithoutExpressionStmtNode.restartKeyword
        val tagNameControl = "tag1"
        val tagNameBlock = "tag2"
        val body =
                "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagNameBlock $keyword${BlockStmtNode.tagPrefix}$tagNameControl ${BlockStmtNode.endToken}"
        val text =
                "${QuantifiedLoopStmtNode.keyword} ${ExplicitQuantifierLexemeNode.startToken}0${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken} $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagNameControl, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test control signals with expression`(keyword: String) {
        val value = LxmInteger.Num10
        val body = "${BlockStmtNode.startToken} $keyword $value ${BlockStmtNode.endToken}"
        val text = "${QuantifiedLoopStmtNode.keyword} ${QuantifierLexemeNode.lazyAbbreviation} $body"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = QuantifiedLoopStmtNode.Companion::parse)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, value) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
