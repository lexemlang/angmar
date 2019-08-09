package org.lexem.angmar.parser.functional.expressions

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import java.util.stream.*
import kotlin.streams.*

internal class AccessExpressionNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = "${ExpressionsCommonsTest.testLiteral}${AccessExplicitMemberNodeTest.testExpression}"

        @JvmStatic
        private fun provideCorrectAccesses(): Stream<Arguments> {
            val elements = listOf(ExpressionsCommonsTest.testLiteral, ExpressionsCommonsTest.testMacro,
                    ParenthesisExpressionNodeTest.testExpression, IdentifierNodeTest.testExpression)
            val modifiers = listOf("", AccessExplicitMemberNodeTest.testExpression, IndexerNodeTest.testExpression,
                    FunctionCallNodeTest.testExpression)
            val sequence = sequence {
                for (element in elements.withIndex()) {
                    for (modifier in modifiers.withIndex()) {
                        for (modifier2 in modifiers.withIndex()) {
                            val text = "${element.value}${modifier.value}${modifier2.value}"

                            yield(Arguments.of(text, element.index, modifier.index, modifier2.index))
                        }

                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is AccessExpressionNode, "The node is not a AccessExpressionNode")
            node as AccessExpressionNode

            Assertions.assertNotNull(node.element, "The element property cannot be null")
            ExpressionsCommonsTest.checkTestLiteral(node.element)
            Assertions.assertEquals(1, node.modifiers.size, "The number of modifiers is incorrect")
            AccessExplicitMemberNodeTest.checkTestExpression(node.modifiers.first())
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCorrectAccesses")
    fun `parse correct accesses`(text: String, elementType: Int, modifierType: Int, modifier2Type: Int) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = AccessExpressionNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")

        if (modifierType == 0 && modifier2Type == 0) {
            res as ParserNode

            when (elementType) {
                0 -> ExpressionsCommonsTest.checkTestLiteral(res)
                1 -> ExpressionsCommonsTest.checkTestMacro(res)
                2 -> ParenthesisExpressionNodeTest.checkTestExpression(res)
                3 -> IdentifierNodeTest.checkTestExpression(res)
                else -> throw AngmarUnimplementedException()
            }
        } else {
            res as AccessExpressionNode

            when (elementType) {
                0 -> ExpressionsCommonsTest.checkTestLiteral(res.element)
                1 -> ExpressionsCommonsTest.checkTestMacro(res.element)
                2 -> ParenthesisExpressionNodeTest.checkTestExpression(res.element)
                3 -> IdentifierNodeTest.checkTestExpression(res.element)
                else -> throw AngmarUnimplementedException()
            }

            val count = if (modifierType != 0 && modifier2Type != 0) {
                2
            } else {
                1
            }
            Assertions.assertEquals(count, res.modifiers.size, "The number of modifiers is incorrect")

            var index = 0
            when (modifierType) {
                0 -> index -= 1
                1 -> AccessExplicitMemberNodeTest.checkTestExpression(res.modifiers[index])
                2 -> IndexerNodeTest.checkTestExpression(res.modifiers[index])
                3 -> FunctionCallNodeTest.checkTestExpression(res.modifiers[index])
                else -> throw AngmarUnimplementedException()
            }

            index += 1

            when (modifier2Type) {
                0 -> Unit
                1 -> AccessExplicitMemberNodeTest.checkTestExpression(res.modifiers[index])
                2 -> IndexerNodeTest.checkTestExpression(res.modifiers[index])
                3 -> FunctionCallNodeTest.checkTestExpression(res.modifiers[index])
                else -> throw AngmarUnimplementedException()
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = AccessExpressionNode.parse(parser)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

