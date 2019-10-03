package org.lexem.angmar.parser.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class PropertyStyleObjectBlockNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression =
                "${PropertyStyleObjectBlockNode.startToken}${CommonsTest.testDynamicIdentifier}${PropertyStyleObjectBlockNode.endToken}"

        @JvmStatic
        private fun provideAllList(): Stream<Arguments> {
            val max = 3
            val sequence = sequence {
                // Only positives
                for (i in 0..max) {
                    val identifiers = List(i) { CommonsTest.testDynamicIdentifier }.joinToString(" ")
                    yield(Arguments.of(identifiers, i, 0, 0))
                }

                // Only negatives
                for (i in 0..max) {
                    val identifiers = List(i) { CommonsTest.testDynamicIdentifier }.joinToString(" ")
                    yield(Arguments.of("${PropertyStyleObjectBlockNode.negativeToken}$identifiers", 0, i, 0))
                }

                // Only sets
                for (i in 0..max) {
                    val identifiers = List(i) { PropertyStyleObjectElementNodeTest.testExpression }.joinToString(" ")
                    yield(Arguments.of("${PropertyStyleObjectBlockNode.setToken}$identifiers", 0, 0, i))
                }

                // Two of them
                for (i in 0..4) {
                    val identifiers = List(i) { CommonsTest.testDynamicIdentifier }.joinToString(" ")
                    val setElements = List(i) { PropertyStyleObjectElementNodeTest.testExpression }.joinToString(" ")

                    yield(Arguments.of("$identifiers${PropertyStyleObjectBlockNode.negativeToken}$identifiers", i, i,
                            0))
                    yield(Arguments.of("$identifiers${PropertyStyleObjectBlockNode.setToken}$setElements", i, 0, i))
                    yield(Arguments.of(
                            "${PropertyStyleObjectBlockNode.negativeToken}$identifiers${PropertyStyleObjectBlockNode.setToken}$setElements",
                            0, i, i))
                }

                // All of them
                for (i in 0..4) {
                    val identifiers = List(i) { CommonsTest.testDynamicIdentifier }.joinToString(" ")
                    val setElements = List(i) { PropertyStyleObjectElementNodeTest.testExpression }.joinToString(" ")

                    yield(Arguments.of(
                            "$identifiers${PropertyStyleObjectBlockNode.negativeToken}$identifiers${PropertyStyleObjectBlockNode.setToken}$setElements",
                            i, i, i))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode) {
            Assertions.assertTrue(node is PropertyStyleObjectBlockNode,
                    "The node is not a PropertyStyleObjectBlockNode")
            node as PropertyStyleObjectBlockNode

            Assertions.assertEquals(1, node.positiveElements.size, "The number of positive elements is incorrect")
            Assertions.assertEquals(0, node.negativeElements.size, "The number of negative elements is incorrect")
            Assertions.assertEquals(0, node.setElements.size, "The number of set elements is incorrect")
            CommonsTest.checkTestDynamicIdentifier(node.positiveElements.first())
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideAllList")
    fun `parse correct prop-style object block with only positives`(identifiers: String, positives: Int, negatives: Int,
            setElements: Int) {
        val text = "${PropertyStyleObjectBlockNode.startToken}$identifiers${PropertyStyleObjectBlockNode.endToken}"
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PropertyStyleObjectBlockNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as PropertyStyleObjectBlockNode

        Assertions.assertEquals(positives, res.positiveElements.size, "The number of positive elements is incorrect")
        Assertions.assertEquals(negatives, res.negativeElements.size, "The number of negative elements is incorrect")
        Assertions.assertEquals(setElements, res.setElements.size, "The number of set elements is incorrect")

        for (el in res.positiveElements) {
            CommonsTest.checkTestDynamicIdentifier(el)
        }

        for (el in res.negativeElements) {
            CommonsTest.checkTestDynamicIdentifier(el)
        }

        for (el in res.setElements) {
            PropertyStyleObjectElementNodeTest.checkTestExpression(el)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @Test
    @Incorrect
    fun `parse incorrect prop-style object block with no endToken`() {
        TestUtils.assertParserException {
            val text = "${PropertyStyleObjectBlockNode.startToken} ${CommonsTest.testDynamicIdentifier}"
            val parser = LexemParser(CustomStringReader.from(text))
            PropertyStyleObjectBlockNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "3"])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(CustomStringReader.from(text))
        val res = PropertyStyleObjectBlockNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}
