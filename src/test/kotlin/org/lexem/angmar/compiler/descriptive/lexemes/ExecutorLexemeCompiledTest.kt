package org.lexem.angmar.compiler.descriptive.lexemes

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.utils.*

internal class ExecutorLexemeCompiledTest {
    @Test
    fun `test not conditional`() {
        val grammar = "${ExecutorLexemeNode.startToken}${LxmInteger.Num2}${ExecutorLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = ExecutorLexemeNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack()
        Assertions.assertEquals(LxmInteger.Num2, result, "The returned value is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test conditional - match`() {
        val grammar =
                "${ExecutorLexemeNode.startToken}${ExecutorLexemeNode.conditionalToken}${LxmLogic.True}${ExecutorLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = ExecutorLexemeNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack()
        Assertions.assertEquals(LxmLogic.True, result, "The returned value is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test conditional - not match`() {
        val grammar =
                "${ExecutorLexemeNode.startToken}${ExecutorLexemeNode.conditionalToken}${LxmLogic.False}${ExecutorLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = ExecutorLexemeNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer, status = LexemAnalyzer.ProcessStatus.Backward, bigNodeCount = 0)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
