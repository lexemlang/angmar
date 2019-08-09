package org.lexem.angmar.parser

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.functional.statements.*
import java.util.stream.*
import kotlin.streams.*

internal class GlobalCommonsTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testBlock = BlockStmtNodeTest.testExpression

        @JvmStatic
        private fun provideBlocks(): Stream<Arguments> {
            val sequence = sequence {
                val tests = listOf(BlockStmtNodeTest.testExpression)

                for (test in tests.withIndex()) {
                    yield(Arguments.of(test.value, test.index))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestBlock(node: ParserNode) = BlockStmtNodeTest.checkTestExpression(node)
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideBlocks")
    fun `parse correct blocks`(text: String, type: Int) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = GlobalCommons.parseBlock(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as ParserNode
        
        // TODO add other possibilities
        when (type) {
            0 -> BlockStmtNodeTest.checkTestExpression(res)
            else -> throw AngmarUnimplementedException()
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }
}
