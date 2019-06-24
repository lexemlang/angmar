package org.lexem.angmar.nodes.expressions.modifiers

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.readers.CustomStringReader

internal class AccessExpressionNodeTest {
    @Test
    fun `parse correct access modifier`() {
        val texts = listOf("id", "if", "return")
        for (test in texts) {
            val text = "${AccessExpressionNode.accessCharacter}$test"
            val res = AccessExpressionNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNotNull(res)
            res as AccessExpressionNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertNotNull(res.identifier, res.toString())
            Assertions.assertNotNull(res.identifier.simpleIdentifiers.first(), test)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse incorrect access modifier`() {
        val texts = listOf("", ".", ". a", ".9")
        for (text in texts) {
            val parser = LexemParser(CustomStringReader.from(text))
            val res = AccessExpressionNode.parse(parser)

            Assertions.assertNull(res)
            Assertions.assertEquals(0, parser.reader.currentPosition())
        }
    }
}