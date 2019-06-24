package org.lexem.angmar.nodes.literals

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.readers.CustomStringReader


internal class NilNodeTest {
    @Test
    fun `parse correct nil keyword`() {
        val texts = listOf(NilNode.nilLiteral, "${NilNode.nilLiteral}-")
        for (text in texts) {
            val res = NilNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNotNull(res)
            res as NilNode

            Assertions.assertEquals(NilNode.nilLiteral, res.content)
            Assertions.assertEquals(NilNode.nilLiteral, res.toString())
        }
    }

    @Test
    fun `parse incorrect nil keyword`() {
        val texts = listOf("${NilNode.nilLiteral}able", "${NilNode.nilLiteral}-able")
        for (text in texts) {
            val parser = LexemParser(CustomStringReader.from(text))
            val res = NilNode.parse(parser)

            Assertions.assertNull(res)
            Assertions.assertEquals(0, parser.reader.currentPosition())
        }
    }
}