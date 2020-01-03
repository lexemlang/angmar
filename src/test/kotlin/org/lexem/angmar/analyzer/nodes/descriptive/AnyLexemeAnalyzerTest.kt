package org.lexem.angmar.analyzer.nodes.descriptive

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class AnyLexemeAnalyzerTest {
    @Test
    fun `test simple lexeme`() {
        val text = "test"
        val grammar = "${StringNode.startToken}$text${StringNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AnyLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test simple lexeme with data capturing`() {
        val dataCapturing = "data"
        val text = "test"
        val grammar =
                "$dataCapturing${AnyLexemeNode.dataCapturingRelationalToken}${StringNode.startToken}$text${StringNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AnyLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val dataCapturingValue =
                context.getDereferencedProperty<LxmString>(analyzer.memory, dataCapturing, toWrite = false)
                        ?: throw Error("The '$dataCapturing' value must be a LxmString")
        Assertions.assertEquals(text, dataCapturingValue.primitive, "The dataCapturingValue is incorrect")
        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node, dataCapturing))
    }

    @Test
    fun `test greedy quantified lexeme with data capturing`() {
        val dataCapturing = "data"
        val text = "aaa"
        val grammar =
                "$dataCapturing${AnyLexemeNode.dataCapturingRelationalToken}${StringNode.startToken}a${StringNode.endToken}${QuantifierLexemeNode.atomicLazyAbbreviations}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AnyLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = text.length + 1)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val dataCapturingValue =
                context.getDereferencedProperty<LxmList>(analyzer.memory, dataCapturing, toWrite = false)
                        ?: throw Error("The '$dataCapturing' value must be a LxmList")

        Assertions.assertEquals(text.length, dataCapturingValue.size, "The number of cells is incorrect")
        for ((i, cell) in dataCapturingValue.getAllCells().withIndex()) {
            Assertions.assertEquals("a", (cell as? LxmString)?.primitive, "The cell[$i] is incorrect")
        }

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node, dataCapturing))
    }

    @Test
    fun `test greedy atomic quantified lexeme with data capturing and backtracking`() {
        val dataCapturing = "data"
        val text = "aaa"
        val grammar =
                "$dataCapturing${AnyLexemeNode.dataCapturingRelationalToken}${StringNode.startToken}aa${StringNode.endToken}${QuantifierLexemeNode.atomicGreedyAbbreviations}${QuantifierLexemeNode.atomicGreedyAbbreviations}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AnyLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 1)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val dataCapturingValue =
                context.getDereferencedProperty<LxmList>(analyzer.memory, dataCapturing, toWrite = false)
                        ?: throw Error("The '$dataCapturing' value must be a LxmList")

        Assertions.assertEquals(1, dataCapturingValue.size, "The number of cells is incorrect")
        for ((i, cell) in dataCapturingValue.getAllCells().withIndex()) {
            Assertions.assertEquals("aa", (cell as? LxmString)?.primitive, "The cell[$i] is incorrect")
        }

        Assertions.assertEquals(text.length - 1, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node, dataCapturing))
    }

    @Test
    fun `test lazy quantified lexeme with data capturing`() {
        val dataCapturing = "data"
        val text = "aaa"
        val grammar =
                "$dataCapturing${AnyLexemeNode.dataCapturingRelationalToken}${StringNode.startToken}a${StringNode.endToken}${QuantifierLexemeNode.atomicLazyAbbreviations}${QuantifierLexemeNode.lazyAbbreviation}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AnyLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val dataCapturingValue =
                context.getDereferencedProperty<LxmList>(analyzer.memory, dataCapturing, toWrite = false)
                        ?: throw Error("The '$dataCapturing' value must be a LxmList")

        Assertions.assertEquals(0, dataCapturingValue.size, "The number of cells is incorrect")
        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has consumed some characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node, dataCapturing))
    }

    @Test
    fun `test lazy atomic quantified lexeme with data capturing and backtracking`() {
        val dataCapturing = "data"
        val text = "aaa"
        val grammar =
                "$dataCapturing${AnyLexemeNode.dataCapturingRelationalToken}${StringNode.startToken}a${StringNode.endToken}${QuantifierLexemeNode.atomicGreedyAbbreviations}${QuantifierLexemeNode.atomicLazyAbbreviations}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AnyLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val dataCapturingValue =
                context.getDereferencedProperty<LxmList>(analyzer.memory, dataCapturing, toWrite = false)
                        ?: throw Error("The '$dataCapturing' value must be a LxmList")

        Assertions.assertEquals(1, dataCapturingValue.size, "The number of cells is incorrect")
        for ((i, cell) in dataCapturingValue.getAllCells().withIndex()) {
            Assertions.assertEquals("a", (cell as? LxmString)?.primitive, "The cell[$i] is incorrect")
        }

        Assertions.assertEquals(1, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node, dataCapturing))
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals without tag`(keyword: String) {
        val grammar = "${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AnyLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from("")

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals with tag`(keyword: String) {
        val tagName = "tag"
        val blockExpression = "$keyword${BlockStmtNode.tagPrefix}$tagName"
        val grammar = "${BlockStmtNode.startToken} $blockExpression ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AnyLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from("")

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagName, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test return control signal`(keyword: String) {
        val value = LxmInteger.Num10
        val blockExpression = "$keyword $value"
        val grammar = "${BlockStmtNode.startToken} $blockExpression ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AnyLexemeNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from("")

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, value) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }
}
