package org.lexem.angmar.compiler.descriptive

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class AnyLexemeCompiledTest {
    @Test
    fun `test simple constant lexeme`() {
        val grammar = "${StringNode.startToken}${StringNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AnyLexemeNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test simple noop lexeme`() {
        val grammar = "${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AnyLexemeNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test simple negative lexeme`() {
        val grammar = "${TextLexemeNode.notOperator}${StringNode.startToken}${StringNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AnyLexemeNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer, status = LexemAnalyzer.ProcessStatus.Backward, bigNodeCount = 0)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    @Incorrect
    fun `test constant to infinity`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.InfiniteLoop) {
            val grammar =
                    "${StringNode.startToken}${StringNode.endToken}${QuantifierLexemeNode.atomicLazyAbbreviations}"
            TestUtils.createAnalyzerFrom(grammar, parserFunction = AnyLexemeNode.Companion::parse)
        }
    }
}
