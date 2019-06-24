package org.lexem.angmar.nodes.expressions

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.readers.CustomStringReader
import org.lexem.angmar.nodes.commons.IdentifierNode
import org.lexem.angmar.nodes.commons.WhitespaceNode
import org.lexem.angmar.nodes.expressions.modifiers.AccessExpressionNode
import org.lexem.angmar.nodes.literals.NumberNode
import org.lexem.angmar.utils.assertNodeType

internal class ExpressionElementNodeTest {
    @Test
    fun `parse correct expression element without modifiers`() {
        val bases = listOf("3")
        for (text in bases) {
            val res = ExpressionElementNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNotNull(res)
            res as ExpressionElementNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertNotNull(res.element)
            assertNodeType<NumberNode>(res.element) { node ->
                Assertions.assertEquals(text, node.toString())
            }
            Assertions.assertEquals(0, res.whitespaces.size)
            Assertions.assertEquals(0, res.modifiers.size)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse correct expression element with one modifier`() {
        // No whites
        let {
            val base = "32"
            val modifier = "modifier"
            val text = "$base${AccessExpressionNode.accessCharacter}$modifier"
            val res = ExpressionElementNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNotNull(res)
            res as ExpressionElementNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertNotNull(res.element)
            assertNodeType<NumberNode>(res.element) { node ->
                Assertions.assertEquals(base, node.toString())
            }
            Assertions.assertEquals(1, res.whitespaces.size)
            assertNodeType<WhitespaceNode>(res.whitespaces.first()) { node ->
                Assertions.assertEquals("", node.toString())
            }
            Assertions.assertEquals(1, res.modifiers.size)
            assertNodeType<AccessExpressionNode>(res.modifiers.first()) { node ->
                Assertions.assertEquals("${AccessExpressionNode.accessCharacter}$modifier", node.toString())
            }
            Assertions.assertEquals(text, res.toString())
        }

        // With whites
        let {
            val base = "id"
            val modifier = "modifier"
            val whitespace = " \n "
            val text = "$base$whitespace${AccessExpressionNode.accessCharacter}$modifier"
            val res = ExpressionElementNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNotNull(res)
            res as ExpressionElementNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertNotNull(res.element)
            assertNodeType<IdentifierNode>(res.element) { node ->
                Assertions.assertEquals(base, node.toString())
            }
            Assertions.assertEquals(1, res.whitespaces.size)
            assertNodeType<WhitespaceNode>(res.whitespaces.first()) { node ->
                Assertions.assertEquals(whitespace, node.toString())
            }
            Assertions.assertEquals(1, res.modifiers.size)
            assertNodeType<AccessExpressionNode>(res.modifiers.first()) { node ->
                Assertions.assertEquals("${AccessExpressionNode.accessCharacter}$modifier", node.toString())
            }
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse incorrect expression element`() {
        val texts = listOf("", "a")
        for (text in texts) {
            val parser = LexemParser(CustomStringReader.from(text))
            val res = AccessExpressionNode.parse(parser)

            Assertions.assertNull(res)
            Assertions.assertEquals(0, parser.reader.currentPosition())
        }
    }
}