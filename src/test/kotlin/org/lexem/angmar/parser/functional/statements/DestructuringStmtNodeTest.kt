package org.lexem.angmar.parser.functional.statements

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class DestructuringStmtNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${DestructuringStmtNode.startToken}${DestructuringElementStmtNodeTest.testExpression}${DestructuringStmtNode.endToken}"

        @JvmStatic
        private fun provideCorrectDestructuringStmt(): Stream<Arguments> {
            val sequence = sequence {
                for (i in 0..2) {
                    val list = List(i) { DestructuringElementStmtNodeTest.testExpression }

                    for (hasAlias in 0..1) {
                        for (hasSpread in 0..1) {
                            for (hasTrailingComma in 0..1) {
                                val currentList = if (hasSpread == 1) {
                                    list + DestructuringSpreadStmtNodeTest.testExpression
                                } else {
                                    list
                                }

                                var text = currentList.joinToString(DestructuringStmtNode.elementSeparator)
                                text = "${DestructuringStmtNode.startToken}$text"

                                if (hasAlias == 1) {
                                    text =
                                            "${IdentifierNodeTest.testExpression}${DestructuringStmtNode.elementSeparator}$text"
                                }

                                if (hasTrailingComma == 1) {
                                    text += DestructuringStmtNode.elementSeparator
                                }

                                text += DestructuringStmtNode.endToken

                                yield(Arguments.of(text, hasAlias == 1, i, hasSpread == 1))

                                // With whitespace
                                text = currentList.joinToString(" ${DestructuringStmtNode.elementSeparator} ")
                                text = "${DestructuringStmtNode.startToken} $text"

                                if (hasAlias == 1) {
                                    text =
                                            "${IdentifierNodeTest.testExpression} ${DestructuringStmtNode.elementSeparator} $text"
                                }

                                if (hasTrailingComma == 1) {
                                    text += " ${DestructuringStmtNode.elementSeparator}"
                                }

                                text += " ${DestructuringStmtNode.endToken}"

                                yield(Arguments.of(text, hasAlias == 1, i, hasSpread == 1))
                            }
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is DestructuringStmtNode, "The node is not a DestructuringStmtNode")
            node as DestructuringStmtNode

            Assertions.assertNull(node.alias, "The alias property must be null")
            Assertions.assertNull(node.spread, "The spread property must be null")

            Assertions.assertEquals(1, node.elements.size, "The number of elments is incorrect")
            DestructuringElementStmtNodeTest.checkTestExpression(node.elements.first())
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectDestructuringStmt")
    fun `parse correct destructuring spread statement`(text: String, hasAlias: Boolean, numElements: Int,
            hasSpread: Boolean) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = DestructuringStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as DestructuringStmtNode

        if (hasAlias) {
            Assertions.assertNotNull(res.alias, "The alias property cannot be null")
            IdentifierNodeTest.checkTestExpression(res.alias!!)
        } else {
            Assertions.assertNull(res.alias, "The alias property must be null")
        }

        if (hasSpread) {
            Assertions.assertNotNull(res.spread, "The spread property cannot be null")
            DestructuringSpreadStmtNodeTest.checkTestExpression(res.spread!!)
        } else {
            Assertions.assertNull(res.spread, "The spread property must be null")
        }

        Assertions.assertEquals(numElements, res.elements.size, "The number of elments is incorrect")

        for (el in res.elements) {
            DestructuringElementStmtNodeTest.checkTestExpression(el)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect destructuring statement without endToken`() {
        TestUtils.assertParserException {
            val text = "${DestructuringStmtNode.startToken}${DestructuringElementStmtNodeTest.testExpression}"
            val parser = LexemParser(CustomStringReader.from(text))
            DestructuringStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", IdentifierNodeTest.testExpression])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = DestructuringStmtNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
