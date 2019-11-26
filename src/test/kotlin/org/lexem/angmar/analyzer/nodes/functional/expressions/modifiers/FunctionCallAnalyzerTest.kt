package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class FunctionCallAnalyzerTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        val positional = listOf(1, 2, 3)
        val named = mapOf("na" to 1, "nb" to 2, "nc" to 3)
        val props = listOf("pa", "pb", "pc")
        val spreadPositional = listOf(4, 5)
        val spreadNamed = mapOf("nd" to 4, "ne" to 5)

        val positionalText = positional.joinToString(FunctionCallNode.argumentSeparator)
        val namedText = named.asSequence().joinToString(FunctionCallNode.argumentSeparator) {
            "${it.key}${FunctionCallNamedArgumentNode.relationalToken}${it.value}"
        }
        val propsText =
                "${FunctionCallExpressionPropertiesNode.relationalToken}${PropertyStyleObjectBlockNode.startToken}${props.joinToString(
                        " ")}${PropertyStyleObjectBlockNode.endToken}"
        val spreadPositionalText = spreadPositional.joinToString(ListNode.elementSeparator)
        val spreadNamedText = spreadNamed.asSequence().joinToString(ObjectNode.elementSeparator) {
            "${it.key}${ObjectElementNode.keyValueSeparator}${it.value}"
        }

        @JvmStatic
        private fun provideAlternatives(): Stream<Arguments> {
            val result = sequence {
                for (i in 0x0..0xF) {
                    val addPositional = i.and(0x1) != 0
                    val addNamed = i.and(0x2) != 0
                    val addSpread = i.and(0x4) != 0
                    val addProperties = i.and(0x8) != 0
                    yield(Arguments.of(addPositional, addNamed, addSpread, addProperties))
                }
            }

            return result.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideAlternatives")
    fun `test call internal function`(addPositional: Boolean, addNamed: Boolean, addSpread: Boolean,
            addProperties: Boolean) {
        val elements = mutableListOf<String>()
        if (addPositional) {
            elements.add(positionalText)
        }

        if (addNamed) {
            elements.add(namedText)
        }

        if (addSpread) {
            elements.add(
                    "${FunctionCallNode.spreadOperator}${ListNode.startToken}$spreadPositionalText${ListNode.endToken}")
            elements.add(
                    "${FunctionCallNode.spreadOperator}${ObjectNode.startToken}$spreadNamedText${ObjectNode.endToken}")
        }

        var finalText = "${FunctionCallNode.startToken}${elements.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        if (addProperties) {
            finalText += propsText
        }

        val analyzer = TestUtils.createAnalyzerFrom(finalText, parserFunction = FunctionCallNode.Companion::parse)

        // Prepare stack
        var executed = false
        val function = LxmInternalFunction { _, arguments, _ ->
            executed = true

            val positionalArguments = arguments.getDereferencedProperty<LxmList>(analyzer.memory,
                    AnalyzerCommons.Identifiers.ArgumentsPositional) ?: throw Error(
                    "The ${AnalyzerCommons.Identifiers.ArgumentsPositional} must be a LxmList")

            val namedArguments = arguments.getDereferencedProperty<LxmObject>(analyzer.memory,
                    AnalyzerCommons.Identifiers.ArgumentsNamed) ?: throw Error(
                    "The ${AnalyzerCommons.Identifiers.ArgumentsNamed} must be a LxmObject")

            val allPositional = positionalArguments.getAllCells()
            val allNamed = namedArguments.getAllIterableProperties()

            // Sizes
            var positionalSize = 0
            var namedSize = 1 // The 'this' parameter.

            // Check positional
            if (addPositional) {
                for (expected in positional.withIndex()) {
                    val value = allPositional[expected.index] as? LxmInteger ?: throw Error(
                            "The positional [${expected.index}] must be a LxmInteger")
                    Assertions.assertEquals(expected.value, value.primitive,
                            "The positional [${expected.index}] is incorrect")
                }

                positionalSize += positional.size
            }

            // Check named
            if (addNamed) {
                for (expected in named) {
                    val value = allNamed[expected.key]?.value as? LxmInteger ?: throw Error(
                            "The named [${expected.key}] must be a LxmInteger")
                    Assertions.assertEquals(expected.value, value.primitive, "The named [${expected.key}] is incorrect")
                }

                namedSize += named.size
            }

            // Check spread
            if (addSpread) {
                for (expected in spreadPositional.withIndex()) {
                    val index = expected.index + positionalSize
                    val value = allPositional[index] as? LxmInteger ?: throw Error(
                            "The positional [${index}] must be a LxmInteger")
                    Assertions.assertEquals(expected.value, value.primitive, "The positional [${index}] is incorrect")
                }

                for (expected in spreadNamed) {
                    val value = allNamed[expected.key]?.value as? LxmInteger ?: throw Error(
                            "The named [${expected.key}] must be a LxmInteger")
                    Assertions.assertEquals(expected.value, value.primitive, "The named [${expected.key}] is incorrect")
                }

                positionalSize += spreadPositional.size
                namedSize += spreadNamed.size
            }

            // Check props
            if (addProperties) {
                val propRef = allNamed[AnalyzerCommons.Identifiers.ArgumentsProperties]!!.value
                val properties = propRef.dereference(analyzer.memory) as LxmObject

                for (p in props) {
                    val value = properties.getPropertyValue(analyzer.memory, p)
                    Assertions.assertEquals(LxmLogic.True, value, "The property $p must be true")
                }

                namedSize += 1
            }

            Assertions.assertEquals(positionalSize, allPositional.size,
                    "The number of positional arguments is incorrect")
            Assertions.assertEquals(namedSize, allNamed.size, "The number of named arguments is incorrect")

            // Always return a value
            analyzer.memory.addToStackAsLast(LxmNil)
            return@LxmInternalFunction true
        }

        analyzer.memory.addToStackAsLast(function)

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertTrue(executed, "The function has not been executed")

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The returned value must be nil")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test call function`() {
        /** Done in [FunctionAnalyzerTest], [FunctionStmtAnalyzerTest] and [ObjectSimplificationAnalyzerTest] **/
    }
}
