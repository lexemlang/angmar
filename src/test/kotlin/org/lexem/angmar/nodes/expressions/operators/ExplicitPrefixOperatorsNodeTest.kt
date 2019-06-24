package org.lexem.angmar.nodes.expressions.operators

import org.junit.jupiter.api.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.errors.AngmarParserException
import org.lexem.angmar.io.readers.CustomStringReader

internal class ExplicitPrefixOperatorsNodeTest {
    @Test
    fun `parse correct graphic prefix operators`() {
        for (text in listOf("+", "-", "-+*")) {
            val res = ExplicitPrefixOperatorsNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as ExplicitPrefixOperatorsNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(1, res.operators.size)
            Assertions.assertEquals(text, res.operators.first().toString())
            Assertions.assertEquals(0, res.whitespaces.size)
            Assertions.assertEquals(text, res.toString())
        }

        for (text in listOf("+${GraphicOperatorNode.operatorSeparator}-", "-${GraphicOperatorNode.operatorSeparator}*",
                "+${GraphicOperatorNode.operatorSeparator}+")) {
            val res = ExplicitPrefixOperatorsNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as ExplicitPrefixOperatorsNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(2, res.operators.size)
            for (op in text.split(GraphicOperatorNode.operatorSeparator).withIndex()) {
                Assertions.assertEquals(op.value, res.operators[op.index].toString())
            }
            Assertions.assertEquals(1, res.whitespaces.size)
            Assertions.assertEquals("", res.whitespaces[0].toString())
            Assertions.assertEquals(text, res.toString())
        }

        for (text in listOf("+${GraphicOperatorNode.operatorSeparator} -${GraphicOperatorNode.operatorSeparator} *",
                "-${GraphicOperatorNode.operatorSeparator} *${GraphicOperatorNode.operatorSeparator} +",
                "+${GraphicOperatorNode.operatorSeparator} +${GraphicOperatorNode.operatorSeparator} -")) {
            val res = ExplicitPrefixOperatorsNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as ExplicitPrefixOperatorsNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(3, res.operators.size)
            for (op in text.split(GraphicOperatorNode.operatorSeparator).withIndex()) {
                Assertions.assertEquals(op.value.trim(), res.operators[op.index].toString())
            }
            Assertions.assertEquals(2, res.whitespaces.size)
            for (i in res.whitespaces) {
                Assertions.assertEquals(" ", i.toString())
            }
            Assertions.assertEquals(" ", res.whitespaces[0].toString())
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse correct textual prefix operators`() {
        for (text in listOf("id${GraphicOperatorNode.operatorSeparator}", "a${GraphicOperatorNode.operatorSeparator}",
                "alfa${GraphicOperatorNode.operatorSeparator}")) {
            val res = ExplicitPrefixOperatorsNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as ExplicitPrefixOperatorsNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(1, res.operators.size)
            Assertions.assertEquals(text.substring(0, text.length - GraphicOperatorNode.operatorSeparator.length),
                    res.operators.first().toString())
            Assertions.assertEquals(0, res.whitespaces.size)
            Assertions.assertEquals(text, res.toString())
        }

        for (text in listOf("x${GraphicOperatorNode.operatorSeparator}id${GraphicOperatorNode.operatorSeparator}",
                "a${GraphicOperatorNode.operatorSeparator}b${GraphicOperatorNode.operatorSeparator}",
                "ahyt${GraphicOperatorNode.operatorSeparator}asdf${GraphicOperatorNode.operatorSeparator}")) {
            val res = ExplicitPrefixOperatorsNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as ExplicitPrefixOperatorsNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(2, res.operators.size)
            for (op in text.split(GraphicOperatorNode.operatorSeparator).filter { it != "" }.withIndex()) {
                Assertions.assertEquals(op.value, res.operators[op.index].toString())
            }
            Assertions.assertEquals(1, res.whitespaces.size)
            Assertions.assertEquals("", res.whitespaces[0].toString())
            Assertions.assertEquals(text, res.toString())
        }

        for (text in listOf(
                "asfwqe${GraphicOperatorNode.operatorSeparator} asnh${GraphicOperatorNode.operatorSeparator} qwetc${GraphicOperatorNode.operatorSeparator}",
                "asdvb${GraphicOperatorNode.operatorSeparator} asdfn${GraphicOperatorNode.operatorSeparator} adsfag${GraphicOperatorNode.operatorSeparator}",
                "id${GraphicOperatorNode.operatorSeparator} zxc${GraphicOperatorNode.operatorSeparator} asd${GraphicOperatorNode.operatorSeparator}")) {
            val res = ExplicitPrefixOperatorsNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as ExplicitPrefixOperatorsNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(3, res.operators.size)
            for (op in text.split(GraphicOperatorNode.operatorSeparator).filter { it != "" }.withIndex()) {
                Assertions.assertEquals(op.value.trim(), res.operators[op.index].toString())
            }
            Assertions.assertEquals(2, res.whitespaces.size)
            for (i in res.whitespaces) {
                Assertions.assertEquals(" ", i.toString())
            }
            Assertions.assertEquals(" ", res.whitespaces[0].toString())
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse correct textual and graphic prefix operators`() {
        for (text in listOf("x${GraphicOperatorNode.operatorSeparator}+",
                "+${GraphicOperatorNode.operatorSeparator}b${GraphicOperatorNode.operatorSeparator}")) {
            val res = ExplicitPrefixOperatorsNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as ExplicitPrefixOperatorsNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(2, res.operators.size)
            for (op in text.split(GraphicOperatorNode.operatorSeparator).filter { it != "" }.withIndex()) {
                Assertions.assertEquals(op.value, res.operators[op.index].toString())
            }
            Assertions.assertEquals(1, res.whitespaces.size)
            Assertions.assertEquals("", res.whitespaces[0].toString())
            Assertions.assertEquals(text, res.toString())
        }

        for (text in listOf(
                "asfwqe${GraphicOperatorNode.operatorSeparator} +${GraphicOperatorNode.operatorSeparator} qwetc${GraphicOperatorNode.operatorSeparator}",
                "+${GraphicOperatorNode.operatorSeparator} asdfn${GraphicOperatorNode.operatorSeparator} -",
                "+${GraphicOperatorNode.operatorSeparator} asdfn${GraphicOperatorNode.operatorSeparator} id${GraphicOperatorNode.operatorSeparator}",
                "id${GraphicOperatorNode.operatorSeparator} zxc${GraphicOperatorNode.operatorSeparator} -")) {
            val res = ExplicitPrefixOperatorsNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
            Assertions.assertNotNull(res, text)
            res as ExplicitPrefixOperatorsNode

            Assertions.assertEquals(text, res.content)
            Assertions.assertEquals(3, res.operators.size)
            for (op in text.split(GraphicOperatorNode.operatorSeparator).filter { it != "" }.withIndex()) {
                Assertions.assertEquals(op.value.trim(), res.operators[op.index].toString())
            }
            Assertions.assertEquals(2, res.whitespaces.size)
            for (i in res.whitespaces) {
                Assertions.assertEquals(" ", i.toString())
            }
            Assertions.assertEquals(" ", res.whitespaces[0].toString())
            Assertions.assertEquals(text, res.toString())
        }
    }

    @Test
    fun `parse incorrect postfix operators`() {
        for (text in listOf("+${GraphicOperatorNode.operatorSeparator}")) {
            Assertions.assertThrows(AngmarParserException::class.java) {
                ExplicitPrefixOperatorsNode.parse(
                    LexemParser(
                        CustomStringReader.from(
                            text
                        )
                    )
                )
            }
        }

        for (text in listOf(GraphicOperatorNode.operatorSeparator)) {
            val parser = LexemParser(CustomStringReader.from(text))
            val res = ExplicitPrefixOperatorsNode.parse(parser)
            Assertions.assertNull(res, text)
            Assertions.assertEquals(0, parser.reader.currentPosition())

        }
    }

    @Test
    @Disabled
    fun `test to see the logs`() {
        try {
            // Change this code by one of the tests above that throw an error.
            val text = "+${GraphicOperatorNode.operatorSeparator}"
            ExplicitPrefixOperatorsNode.parse(
                LexemParser(
                    CustomStringReader.from(
                        text
                    )
                )
            )
        } catch (e: AngmarParserException) {
            e.logMessage()
        }
    }
}