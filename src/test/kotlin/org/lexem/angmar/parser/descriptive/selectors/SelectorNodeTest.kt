package org.lexem.angmar.parser.descriptive.selectors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import java.util.stream.*
import kotlin.streams.*

internal class SelectorNodeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        const val testExpression = NameSelectorNodeTest.testExpression

        @JvmStatic
        private fun provideSelectors(): Stream<Arguments> {
            val sequence = sequence {
                for (hasName in listOf(false, true)) {
                    for (propCount in 0..2) {
                        for (methodCount in 0..2) {
                            if (!hasName && propCount == 0 && methodCount == 0) {
                                continue
                            }

                            val list = mutableListOf<String>()
                            if (hasName) {
                                list.add(NameSelectorNodeTest.testExpression)
                            }
                            list.addAll(List(propCount) { PropertySelectorNodeTest.testExpression })
                            list.addAll(List(methodCount) { MethodSelectorNodeTest.testExpression })

                            yield(Arguments.of(list.joinToString(""), hasName, propCount, methodCount))

                            // With whitespaces
                            yield(Arguments.of(list.joinToString(" "), hasName, propCount, methodCount))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provideAdditionSelectors(): Stream<Arguments> {
            val sequence = sequence {
                for (propCount in 0..2) {
                    val list = mutableListOf<String>()
                    list.add(NameSelectorNodeTest.testExpression)
                    list.addAll(List(propCount) { PropertySelectorNodeTest.testExpression })

                    yield(Arguments.of(list.joinToString(""), propCount))

                    // With whitespaces
                    yield(Arguments.of(list.joinToString(" "), propCount))
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        fun checkTestExpression(node: ParserNode, isAddition: Boolean) {
            Assertions.assertNotNull(node, "The input has not been correctly parsed")
            node as SelectorNode

            Assertions.assertEquals(isAddition, node.isAddition, "The isAddition property is incorrect")
            Assertions.assertNotNull(node.name, "The arguments property cannot be null")
            NameSelectorNodeTest.checkTestExpression(node.name!!, isAddition)

            Assertions.assertEquals(0, node.properties.size, "The property count is incorrect")
            Assertions.assertEquals(0, node.methods.size, "The method count is incorrect")
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideSelectors")
    fun `parse correct selector`(text: String, hasName: Boolean, propCount: Int, methodCount: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = SelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as SelectorNode

        Assertions.assertFalse(res.isAddition, "The isAddition property is incorrect")
        Assertions.assertEquals(propCount, res.properties.size, "The property count is incorrect")

        if (hasName) {
            Assertions.assertNotNull(res.name, "The arguments property cannot be null")
            NameSelectorNodeTest.checkTestExpression(res.name!!, isAddition = false)
        } else {
            Assertions.assertNull(res.name, "The arguments property must be null")
        }

        Assertions.assertEquals(propCount, res.properties.size, "The property count is incorrect")

        res.properties.forEach {
            PropertySelectorNodeTest.checkTestExpression(it, isAddition = false)
        }

        Assertions.assertEquals(methodCount, res.methods.size, "The method count is incorrect")

        res.methods.forEach {
            MethodSelectorNodeTest.checkTestExpression(it)
        }

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @MethodSource("provideAdditionSelectors")
    fun `parse correct selector - for addition`(text: String, propCount: Int) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = SelectorNode.parseForAddition(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as SelectorNode

        Assertions.assertTrue(res.isAddition, "The isAddition property is incorrect")

        Assertions.assertNotNull(res.name, "The arguments property cannot be null")
        NameSelectorNodeTest.checkTestExpression(res.name!!, isAddition = true)

        Assertions.assertEquals(propCount, res.properties.size, "The property count is incorrect")

        res.properties.forEach {
            PropertySelectorNodeTest.checkTestExpression(it, isAddition = true)
        }

        Assertions.assertEquals(0, res.methods.size, "The method count is incorrect")

        Assertions.assertEquals(text.length, parser.reader.currentPosition(), "The parser did not advance the cursor")
    }

    @ParameterizedTest
    @ValueSource(strings = [""])
    fun `not parse the node`(text: String) {
        val parser = LexemParser(IOStringReader.from(text))
        val res = SelectorNode.parse(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNull(res, "The input has incorrectly parsed anything")
        Assertions.assertEquals(0, parser.reader.currentPosition(), "The parser must not advance the cursor")
    }
}

