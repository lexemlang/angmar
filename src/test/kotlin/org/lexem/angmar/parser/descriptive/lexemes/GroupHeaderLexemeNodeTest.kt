package org.lexem.angmar.parser.descriptive.lexemes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*
import java.util.stream.*
import kotlin.streams.*

internal class GroupHeaderLexemeNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = IdentifierNodeTest.testExpression

        @JvmStatic
        private fun provideNodes(): Stream<Arguments> {
            val sequence = sequence {
                for (hasQuantifier in listOf(false, true)) {
                    for (hasIdentifier in listOf(false, true)) {
                        for (hasPropertyBlock in listOf(false, true)) {
                            if (!hasQuantifier && !hasIdentifier && !hasPropertyBlock) {
                                continue
                            }

                            val list = mutableListOf<String>()

                            if (hasQuantifier) {
                                list.add(QuantifierLexemeNodeTest.testExpression)
                            }

                            if (hasIdentifier) {
                                list.add(IdentifierNodeTest.testExpression)
                            }

                            if (hasPropertyBlock) {
                                list.add(PropertyStyleObjectBlockNodeTest.testExpression)
                            }

                            yield(Arguments.of(list.joinToString(""), hasQuantifier, hasIdentifier, hasPropertyBlock))

                            // With whitespaces
                            yield(Arguments.of(list.joinToString(" "), hasQuantifier, hasIdentifier, hasPropertyBlock))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as GroupHeaderLexemeNode

            Assertions.assertNull(node.quantifier, "The quantifier property must be null")
            Assertions.assertNotNull(node.identifier, "The identifier property cannot be null")
            IdentifierNodeTest.checkTestExpression(node.identifier!!)
            Assertions.assertNull(node.propertyBlock, "The propertyBlock property must be null")
        }
    }


    // TESTS ------------------------------------------------------------------


    @ParameterizedTest
    @MethodSource("provideNodes")
    fun `parse correct node`(text: String, hasQuantifier: Boolean, hasIdentifier: Boolean, hasPropertyBlock: Boolean) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = GroupHeaderLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as GroupHeaderLexemeNode

        if (hasQuantifier) {
            Assertions.assertNotNull(res.quantifier, "The quantifier property cannot be null")
            QuantifierLexemeNodeTest.checkTestExpression(res.quantifier!!)
        } else {
            Assertions.assertNull(res.quantifier, "The quantifier property must be null")
        }

        if (hasIdentifier) {
            Assertions.assertNotNull(res.identifier, "The identifier property cannot be null")
            IdentifierNodeTest.checkTestExpression(res.identifier!!)
        } else {
            Assertions.assertNull(res.identifier, "The identifier property must be null")
        }

        if (hasPropertyBlock) {
            Assertions.assertNotNull(res.propertyBlock, "The propertyBlock property cannot be null")
            PropertyStyleObjectBlockNodeTest.checkTestExpression(res.propertyBlock!!)
        } else {
            Assertions.assertNull(res.propertyBlock, "The propertyBlock property must be null")
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = GroupHeaderLexemeNode.parse(parser, ParserNode.Companion.EmptyParserNode)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

