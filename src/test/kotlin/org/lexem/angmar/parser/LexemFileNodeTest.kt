package org.lexem.angmar.parser

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class LexemFileNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        @JvmStatic
        private fun provideCorrectStatements(): Stream<Arguments> {
            val result = sequence {
                yield(Arguments.of("", 0))
                yield(Arguments.of("   ", 0))

                for (stmt in StatementCommonsTest.statements) {
                    yield(Arguments.of(stmt, 1))
                }

                for (eol in WhitespaceNode.endOfLineChars) {
                    yield(Arguments.of(StatementCommonsTest.statements.joinToString("$eol"),
                            StatementCommonsTest.statements.size))
                }

                yield(Arguments.of(StatementCommonsTest.statements.joinToString(WhitespaceNode.windowsEndOfLine),
                        StatementCommonsTest.statements.size))
            }

            return result.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectStatements")
    fun `parse correct Lexem file`(text: String, stmtNum: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = LexemFileNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LexemFileNode

        Assertions.assertEquals(stmtNum, res.statements.size, "The number of statements is incorrect")

        for (stmt in res.statements.withIndex()) {
            Assertions.assertNotNull(stmt.value, "The statement[${stmt.index}] cannot be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect Lexem file without end of file`() {
        TestUtils.assertParserException(AngmarParserExceptionType.LexemFileEOFExpected) {
            val text = "∫ another text \nthat do not match"
            val parser = LexemParser(IOStringReader.from(text))
            LexemFileNode.parse(parser)
        }
    }

    @Test
    fun `not parse the node`() {
        val prefix = "a"
        val text = "$prefix test"
        val parser = LexemParser(IOStringReader.from(text))
        parser.readText(prefix)
        val res = LexemFileNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(prefix.length, parser.reader.currentPosition(),
                "The parser must not advance the cursor")
    }
}
