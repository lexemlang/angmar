package org.lexem.angmar.analyzer.nodes.descriptive

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class LexemePatternGroupAnalyzerTest {
    @Test
    fun `test alternative patterns - last pattern`() {
        val text = "c"
        val grammar = generatePatterns(listOf("a", "b", text))
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The patterns have not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test additive patterns - last pattern`() {
        val text = "c"
        val grammar = generatePatterns(listOf("a", "b", text), type = LexemePatternNode.Companion.PatternType.Additive)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The patterns have not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test selective patterns - last pattern`() {
        val text = "c"
        val grammar = generatePatterns(listOf("a", "b", text), type = LexemePatternNode.Companion.PatternType.Selective)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The patterns have not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test quantified patterns - last pattern`() {
        val text = "c"
        val grammar = generateQuantifiedPatterns(listOf("a", "b", text), 1)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The patterns have not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test patterns - empty first pattern`() {
        val text = "c"
        val grammar = generateQuantifiedPatterns(listOf(null, "b", text), 1)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The patterns have consumed some characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test patterns - multiple patterns`() {
        val text = "ac"
        val grammar = generateQuantifiedPatterns(listOf("a", "b", "c"), 2)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 3)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The patterns have consumed some characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test patterns - not matching`() {
        val text = "x"
        val grammar = generatePatterns(listOf("b"))
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The patterns have consumed some characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    @Incorrect
    fun `test incorrect quantifier minimum`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.QuantifierMinimumIsGreaterThanNumberOfPatterns) {
            val variableName = "testVariable"
            val text = "ab"
            val grammar = generateQuantifiedPatterns(listOf("a", "b"), variableName)
            val analyzer =
                    TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                            isDescriptiveCode = true)
            val textReader = IOStringReader.from(text)

            // Prepare context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
            context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)
            context.setPropertyAsContext(analyzer.memory, variableName, LxmInteger.from(4))

            TestUtils.processAndCheckEmpty(analyzer, textReader)
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals without tag`(keyword: String) {
        val grammar = "${LexemePatternNode.patternToken} ${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from("")

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals with tag`(keyword: String) {
        val tagName = "tag"
        val blockExpression = "$keyword${BlockStmtNode.tagPrefix}$tagName"
        val grammar =
                "${LexemePatternNode.patternToken} ${BlockStmtNode.startToken} $blockExpression ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from("")

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagName, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test return control signal`(keyword: String) {
        val value = LxmInteger.Num10
        val blockExpression = "$keyword $value"
        val grammar =
                "${LexemePatternNode.patternToken} ${BlockStmtNode.startToken} $blockExpression ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternGroupNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from("")

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, node)

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, value) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    // AUXILIARY FUNCTIONS ----------------------------------------------------

    companion object {
        internal fun generatePatterns(texts: List<String?>,
                type: LexemePatternNode.Companion.PatternType = LexemePatternNode.Companion.PatternType.Alternative) =
                texts.joinToString("\n") {
                    "${LexemePatternNode.patternToken}${type.token} ${printText(it)}"
                }

        fun generateQuantifiedPatterns(texts: List<String?>, min: Int, isInfinite: Boolean = false) =
                generateQuantifiedPatterns(texts, min.toString(), isInfinite)

        fun generateQuantifiedPatterns(texts: List<String?>, min: String, isInfinite: Boolean = false) =
                StringBuilder().apply {
                    if (isInfinite) {
                        append("${LexemePatternNode.patternToken}${ExplicitQuantifierLexemeNode.startToken}$min${ExplicitQuantifierLexemeNode.elementSeparator}${ExplicitQuantifierLexemeNode.endToken} ${printText(
                                texts.first())}")
                    } else {
                        append("${LexemePatternNode.patternToken}${ExplicitQuantifierLexemeNode.startToken}$min${ExplicitQuantifierLexemeNode.endToken} ${printText(
                                texts.first())}")
                    }

                    if (texts.size > 1) {
                        append("\n")
                        append(texts.drop(1).joinToString("\n") {
                            "${LexemePatternNode.patternToken}${LexemePatternNode.quantifierSlaveToken} ${printText(
                                    it)}"
                        })
                    }
                }.toString()

        private fun printText(text: String?) = if (text == null) {
            ""
        } else {
            "${StringNode.startToken}${text}${StringNode.endToken}"
        }
    }
}
