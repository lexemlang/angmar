package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class GroupHeaderLexemAnalyzerTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        private val nodeName = "nodeName"
        private val affirmative = AnalyzerCommons.Properties.Reverse
        private val negative = "test"
        private val set = AnalyzerCommons.Properties.Backtrack
        private val setValue = LxmInteger.Num10

        @JvmStatic
        private fun provideCombinations(): Stream<Arguments> {
            val sequence = sequence {
                for (quantifier in listOf(null, LxmQuantifier.GreedyZeroOrOne)) {
                    for (name in listOf(null, nodeName)) {
                        for (properties in listOf(null,
                                mapOf(affirmative to LxmLogic.True, negative to LxmLogic.False, set to setValue))) {
                            // Skip empty
                            if (quantifier == null && name == null && properties == null) {
                                continue
                            }

                            var text = if (quantifier != null) {
                                QuantifierLexemeNode.lazyAbbreviation
                            } else {
                                ""
                            }

                            if (name != null) {
                                text += nodeName
                            }

                            if (properties != null) {
                                text += "${PropertyStyleObjectBlockNode.startToken}$affirmative ${PropertyStyleObjectBlockNode.negativeToken}$negative ${PropertyStyleObjectBlockNode.setToken} $set${ParenthesisExpressionNode.startToken}$setValue${ParenthesisExpressionNode.endToken}${PropertyStyleObjectBlockNode.endToken}"
                            }

                            yield(Arguments.of(text, quantifier, name, properties))
                        }
                    }
                }
            }

            return sequence.asStream()
        }

        // AUX METHODS --------------------------------------------------------

        /**
         * Checks all the results of the header.
         */
        private fun checkHeader(analyzer: LexemAnalyzer, quantifier: LxmQuantifier?, nodeName: String?,
                properties: Map<String, LexemMemoryValue>?) {
            val union =
                    analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(analyzer.memory,
                            toWrite = false) as? LxmPatternUnion ?: throw Error(
                            "The ${AnalyzerCommons.Identifiers.LexemeUnion} must be a LxmPatternUnion")

            if (quantifier == null) {
                Assertions.assertTrue(union.quantifierIsEqualsTo(LxmQuantifier.AlternativePattern),
                        "The quantifier property is incorrect")
                Assertions.assertEquals(LxmInteger.Num0, union.getIndex(analyzer.memory),
                        "The index property is incorrect")
            } else {
                Assertions.assertTrue(union.quantifierIsEqualsTo(quantifier), "The quantifier property is incorrect")
                Assertions.assertEquals(LxmInteger.Num0, union.getIndex(analyzer.memory),
                        "The index property is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            val lxmNodeRef = context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Node)!!
            val lxmNode = lxmNodeRef.dereference(analyzer.memory, toWrite = false) as LxmNode
            if (nodeName == null) {
                Assertions.assertEquals("", lxmNode.name, "The name property is incorrect")
            } else {
                Assertions.assertEquals(nodeName, lxmNode.name, "The name property is incorrect")
            }

            val finalProps: MutableMap<String, LexemMemoryValue> =
                    hashMapOf(AnalyzerCommons.Properties.Children to LxmLogic.True,
                            AnalyzerCommons.Properties.Backtrack to LxmLogic.True,
                            AnalyzerCommons.Properties.Consume to LxmLogic.True,
                            AnalyzerCommons.Properties.Capture to LxmLogic.False,
                            AnalyzerCommons.Properties.Property to LxmLogic.False,
                            AnalyzerCommons.Properties.Insensible to LxmLogic.False,
                            AnalyzerCommons.Properties.Reverse to LxmLogic.False)

            if (properties != null) {
                finalProps.putAll(properties)
            }

            val props = AnalyzerCommons.getCurrentNodeProps(analyzer.memory, toWrite = false)
            for ((name, prop) in finalProps) {
                val actual = props.getDereferencedProperty<LexemMemoryValue>(analyzer.memory, name, toWrite = false)
                Assertions.assertEquals(prop, actual, "The property called $name is incorrect")
            }

            Assertions.assertEquals(finalProps.size, props.size, "The property count is incorrect")

            // Remove LexemeUnion from the stack.
            analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LexemeUnion)

            // Remove the node to avoid circular references.
            val parentNode = lxmNode.getParent(analyzer.memory, toWrite = false)!!
            val parentChildren = parentNode.getChildren(analyzer.memory, toWrite = false)
            parentChildren.removeCell(analyzer.memory, parentChildren.size - 1, ignoreConstant = true)
            context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node,
                    lxmNode.getParentReference(analyzer.memory)!!, ignoreConstant = true)
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideCombinations")
    fun `test all possibilities`(text: String, quantifier: LxmQuantifier?, nodeName: String?,
            properties: Map<String, LexemMemoryValue>?) {
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = GroupHeaderLexemeNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        checkHeader(analyzer, quantifier, nodeName, properties)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }
}
