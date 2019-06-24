package org.lexem.angmar.nodes.expressions.operators

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.readers.CustomStringReader

internal class InfixOperatorNodeTest {
    @Test
    fun `parse graphic infix operator`() {
        for (text in GraphicOperatorNodeTest.builtInGraphicOperator) {
            val res = InfixOperatorNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNotNull(res, text)
            res as InfixOperatorNode

            Assertions.assertNotNull(res.graphic)
            Assertions.assertTrue(res.isGraphic)
            Assertions.assertEquals(text, res.graphic!!.operator)
            Assertions.assertNull(res.identifier)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse identifier-like infix operator`() {
        for (text in listOf("x", "add", "substract", "until", "_", "xx_xx")) {
            val res = InfixOperatorNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNotNull(res, text)
            res as InfixOperatorNode

            Assertions.assertNull(res.graphic)
            Assertions.assertFalse(res.isGraphic)
            Assertions.assertNotNull(res.identifier)
            Assertions.assertEquals(text, res.identifier!!.content)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse incorrect infix operator`() {
        for (operator in GraphicOperatorNodeTest.builtInGraphicOperator) {
            val text = "$operator${GraphicOperatorNode.operatorSeparator}"
            val parser = LexemParser(CustomStringReader.from(text))
            val res = InfixOperatorNode.parse(parser)

            Assertions.assertNull(res, text)
            Assertions.assertEquals(0, parser.reader.currentPosition())
        }

        for (operator in listOf("x", "add", "substract", "until", "_", "xx_xx")) {
            val text = "$operator${GraphicOperatorNode.operatorSeparator}"
            val parser = LexemParser(CustomStringReader.from(text))
            val res = InfixOperatorNode.parse(parser)

            Assertions.assertNull(res, text)
            Assertions.assertEquals(0, parser.reader.currentPosition())
        }
    }
}