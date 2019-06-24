package org.lexem.angmar.nodes.expressions.operators

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.readers.CustomStringReader

internal class GraphicOperatorNodeTest {
    @Test
    fun `parse correct graphic operator`() {
        for (headCharRange in GraphicOperatorNode.operatorHeadChars) {
            val text = "${((headCharRange.last.toInt() + headCharRange.first.toInt()) / 2).toChar()}"
            val res = GraphicOperatorNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNotNull(res, text)
            res as GraphicOperatorNode

            Assertions.assertEquals(text, res.operator)
            Assertions.assertEquals(text, res.toString())
        }

        for (headCharRange in GraphicOperatorNode.operatorHeadChars) {
            val headChar = "${((headCharRange.last.toInt() + headCharRange.first.toInt()) / 2).toChar()}"

            for (nextCharRange in GraphicOperatorNode.operatorHeadChars) {
                val nextChar = "${((nextCharRange.last.toInt() + nextCharRange.first.toInt()) / 2).toChar()}"

                val text = "$headChar$nextChar"
                val res = GraphicOperatorNode.parse(
                    LexemParser(
                        CustomStringReader.from(
                            text
                        )
                    )
                )

                Assertions.assertNotNull(res, text)
                res as GraphicOperatorNode

                Assertions.assertEquals(text, res.operator)
                Assertions.assertEquals(text, res.toString())
            }

            for (nextCharRange in GraphicOperatorNode.operatorEndChars) {
                val nextChar = "${((nextCharRange.last.toInt() + nextCharRange.first.toInt()) / 2).toChar()}"

                val text = "$headChar$nextChar"
                val res = GraphicOperatorNode.parse(
                    LexemParser(
                        CustomStringReader.from(
                            text
                        )
                    )
                )

                Assertions.assertNotNull(res, text)
                res as GraphicOperatorNode

                Assertions.assertEquals(text, res.operator)
                Assertions.assertEquals(text, res.toString())
            }
        }
    }

    @Test
    fun `parse correct graphic dot-operator`() {
        for (headCharRange in GraphicOperatorNode.operatorHeadChars) {
            val text = ".${((headCharRange.last.toInt() + headCharRange.first.toInt()) / 2).toChar()}"
            val res = GraphicOperatorNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNotNull(res, text)
            res as GraphicOperatorNode

            Assertions.assertEquals(text, res.operator)
            Assertions.assertEquals(text, res.toString())
        }

        for (headCharRange in GraphicOperatorNode.operatorHeadChars) {
            val headChar = "${((headCharRange.last.toInt() + headCharRange.first.toInt()) / 2).toChar()}"

            for (nextCharRange in GraphicOperatorNode.operatorHeadChars) {
                val nextChar = "${((nextCharRange.last.toInt() + nextCharRange.first.toInt()) / 2).toChar()}"

                val text = ".$headChar$nextChar"
                val res = GraphicOperatorNode.parse(
                    LexemParser(
                        CustomStringReader.from(
                            text
                        )
                    )
                )

                Assertions.assertNotNull(res, text)
                res as GraphicOperatorNode

                Assertions.assertEquals(text, res.operator)
                Assertions.assertEquals(text, res.toString())
            }

            for (nextCharRange in GraphicOperatorNode.operatorEndChars) {
                val nextChar = "${((nextCharRange.last.toInt() + nextCharRange.first.toInt()) / 2).toChar()}"

                val text = ".$headChar$nextChar"
                val res = GraphicOperatorNode.parse(
                    LexemParser(
                        CustomStringReader.from(
                            text
                        )
                    )
                )

                Assertions.assertNotNull(res, text)
                res as GraphicOperatorNode

                Assertions.assertEquals(text, res.operator)
                Assertions.assertEquals(text, res.toString())
            }
        }
    }

    @Test
    fun `parse built-in graphic operators`() {
        for (text in builtInGraphicOperator) {
            val res = GraphicOperatorNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )

            Assertions.assertNotNull(res, text)
            res as GraphicOperatorNode

            Assertions.assertEquals(text, res.operator)
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse incorrect graphic operator`() {
        for (nextCharRange in GraphicOperatorNode.operatorEndChars) {
            for (nextChar in nextCharRange) {
                val text = "$nextChar"
                val parser = LexemParser(CustomStringReader.from(text))
                val res = GraphicOperatorNode.parse(parser)

                Assertions.assertNull(res,
                        "[${nextCharRange.start.toInt().toString(16)}, ${nextCharRange.last.toInt().toString(
                                16)}] ${nextChar.toInt().toString(16)}")
                Assertions.assertEquals(0, parser.reader.currentPosition())
            }
        }

        for (text in listOf(".")) {
            val parser = LexemParser(CustomStringReader.from(text))
            val res = GraphicOperatorNode.parse(parser)

            Assertions.assertNull(res, text)
            Assertions.assertEquals(0, parser.reader.currentPosition())
        }
    }

    companion object {
        val builtInGraphicOperator = listOf("+", "-", "%", "*", "/", "...", ".-+", "..>", "<..>", "<..")
    }
}