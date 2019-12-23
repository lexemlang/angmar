package org.lexem.angmar.compiler.functional.statements

import org.junit.jupiter.api.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.utils.*

internal class BlockStmtCompiledTest {
    @Test
    fun `test empty block with constant tag`() {
        val tagName = "tag"
        val text = "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
