package org.lexem.angmar.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.readers.CustomStringReader

internal class BooleanNodeTest {
    @Test
    fun `parse correct true and false keyword`() {
        var texts = listOf(BooleanNode.trueLiteral, "${BooleanNode.trueLiteral}-")
        for (text in texts) {
            val res = BooleanNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNotNull(res)
            res as BooleanNode

            Assertions.assertEquals(BooleanNode.trueLiteral, res.content)
            Assertions.assertEquals(BooleanNode.trueLiteral, res.toString())
        }

        texts = listOf(BooleanNode.falseLiteral, "${BooleanNode.falseLiteral}-")
        for (text in texts) {
            val res = BooleanNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNotNull(res)
            res as BooleanNode

            Assertions.assertEquals(BooleanNode.falseLiteral, res.content)
            Assertions.assertEquals(BooleanNode.falseLiteral, res.toString())
        }
    }

    @Test
    fun `parse incorrect true and false keyword`() {
        val texts = listOf("${BooleanNode.trueLiteral}able", "${BooleanNode.trueLiteral}-able",
                "${BooleanNode.falseLiteral}able", "${BooleanNode.falseLiteral}-able")
        for (text in texts) {
            val parser = LexemParser(CustomStringReader.from(text))
            val res = BooleanNode.parse(parser)

            Assertions.assertNull(res)
            Assertions.assertEquals(0, parser.reader.currentPosition())
        }
    }
}