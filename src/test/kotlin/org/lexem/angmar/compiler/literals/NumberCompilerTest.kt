package org.lexem.angmar.compiler.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class NumberCompilerTest {
    @Test
    @Incorrect
    fun `test too long number`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.NumberOverflow) {
            val text = "8174509634562345692364592360456293456923"
            val analyzer = TestUtils.createAnalyzerFrom(text,
                    parserFunction = NumberNode.Companion::parseAnyNumberDefaultDecimal)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
