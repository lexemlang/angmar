package org.lexem.angmar.nodes.expressions

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.readers.CustomStringReader
import org.lexem.angmar.nodes.commons.IdentifierNode
import org.lexem.angmar.nodes.commons.WhitespaceNode
import org.lexem.angmar.utils.assertNodeType

internal class SemanticIdentifierListNodeTest {
    @Test
    fun `parse correct semantic identifier list of one element`() {
        val texts = listOf("a", "id", "this_is_a_long_test", "`this is a quoted id`")
        for (text in texts) {
            val res = SemanticIdentifierListNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNotNull(res)
            res as SemanticIdentifierListNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(1, res.identifiers.size)
            assertNodeType<IdentifierNode>(res.identifiers.first()) { node ->
                Assertions.assertEquals(text, node.toString())
            }
            Assertions.assertEquals(0, res.whitespaces.size)
            Assertions.assertEquals(text, res.toString())
        }

        for (text in IdentifierNode.keywords.map { "${it}a" }) {
            val res = SemanticIdentifierListNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNotNull(res)
            res as SemanticIdentifierListNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(1, res.identifiers.size)
            assertNodeType<IdentifierNode>(res.identifiers.first()) { node ->
                Assertions.assertEquals(text, node.toString())
            }
            Assertions.assertEquals(0, res.whitespaces.size)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse correct semantic identifier list of more than one element`() {
        var whitespace = " "
        val ids = listOf("a", "id", "this_is_a_long_test", "`this is a quoted id`")
        for (id1 in ids) {
            for (id2 in ids) {
                val text = "$id1$whitespace$id2"
                val res = SemanticIdentifierListNode.parse(
                    LexemParser(
                        CustomStringReader.from(
                            text
                        )
                    )
                )

                Assertions.assertNotNull(res)
                res as SemanticIdentifierListNode

                Assertions.assertEquals(text, res.content)
                Assertions.assertEquals(2, res.identifiers.size)
                assertNodeType<IdentifierNode>(res.identifiers.first()) { node ->
                    Assertions.assertEquals(id1, node.toString())
                }
                assertNodeType<IdentifierNode>(res.identifiers[1]) { node ->
                    Assertions.assertEquals(id2, node.toString())
                }
                Assertions.assertEquals(1, res.whitespaces.size)
                assertNodeType<WhitespaceNode>(res.whitespaces.first()) { node ->
                    Assertions.assertEquals(whitespace, node.toString())
                }
                Assertions.assertEquals(text, res.toString())
            }
        }

        whitespace = " \n\t"
        for (id1 in ids) {
            for (id2 in ids) {
                val text = "$id1$whitespace$id2"
                val res = SemanticIdentifierListNode.parse(
                    LexemParser(
                        CustomStringReader.from(
                            text
                        )
                    )
                )

                Assertions.assertNotNull(res)
                res as SemanticIdentifierListNode

                Assertions.assertEquals(text, res.content)
                Assertions.assertEquals(2, res.identifiers.size)
                assertNodeType<IdentifierNode>(res.identifiers.first()) { node ->
                    Assertions.assertEquals(id1, node.toString())
                }
                assertNodeType<IdentifierNode>(res.identifiers[1]) { node ->
                    Assertions.assertEquals(id2, node.toString())
                }
                Assertions.assertEquals(1, res.whitespaces.size)
                assertNodeType<WhitespaceNode>(res.whitespaces.first()) { node ->
                    Assertions.assertEquals(whitespace, node.toString())
                }
                Assertions.assertEquals(text, res.toString())
            }
        }
    }

    @Test
    fun `not fully parse the semantic identifier list`() {
        let {
            val text = "a b \n true"
            val parser = LexemParser(CustomStringReader.from(text))
            val res = SemanticIdentifierListNode.parse(parser)

            Assertions.assertNotNull(res)
            res as SemanticIdentifierListNode

            Assertions.assertEquals("a b", res.content)
            Assertions.assertEquals(2, res.identifiers.size)
            assertNodeType<IdentifierNode>(res.identifiers.first()) { node ->
                Assertions.assertEquals("a", node.toString())
            }
            assertNodeType<IdentifierNode>(res.identifiers[1]) { node ->
                Assertions.assertEquals("b", node.toString())
            }
            Assertions.assertEquals(1, res.whitespaces.size)
            assertNodeType<WhitespaceNode>(res.whitespaces.first()) { node ->
                Assertions.assertEquals(" ", node.toString())
            }
            Assertions.assertEquals("a b", res.toString())
        }
    }


    @Test
    fun `parse incorrect semantic identifier list`() {
        let {
            val text = ""
            val parser = LexemParser(CustomStringReader.from(text))
            val res = SemanticIdentifierListNode.parse(parser)

            Assertions.assertNull(res)
            Assertions.assertEquals(0, parser.reader.currentPosition())
        }

        for (text in IdentifierNode.keywords) {
            val parser = LexemParser(CustomStringReader.from(text))
            val res = SemanticIdentifierListNode.parse(parser)

            Assertions.assertNull(res)
            Assertions.assertEquals(0, parser.reader.currentPosition())
        }
    }
}