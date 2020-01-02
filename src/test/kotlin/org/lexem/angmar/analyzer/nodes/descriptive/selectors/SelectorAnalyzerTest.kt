package org.lexem.angmar.analyzer.nodes.descriptive.selectors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.descriptive.selectors.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class SelectorAnalyzerTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        @JvmStatic
        private fun provideFilterOptions(): Stream<Arguments> {
            val sequence = sequence {
                for (isOk in listOf(false, true)) {
                    for (hasName in listOf(false, true)) {
                        for (hasProperty in listOf(false, true)) {
                            for (hasMethod in listOf(false, true)) {
                                if (!(hasName || hasProperty || hasMethod)) {
                                    continue
                                }

                                yield(Arguments.of(isOk, hasName, hasProperty, hasMethod))
                            }
                        }
                    }
                }
            }

            return sequence.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @Test
    fun `test addition - only name`() {
        val nodeName = "nodeName"
        val grammar = nodeName
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = SelectorNode.Companion::parseForAddition)

        TestUtils.processAndCheckEmpty(analyzer)

        analyzer.memory.getLastFromStack().dereference(analyzer.memory, toWrite = false) as? LxmNode ?: throw Error(
                "The result must be a LxmNode")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test addition - with properties`() {
        val nodeName = "nodeName"
        val propName1 = "propName1"
        val propName2 = "propName2"
        val grammar =
                "$nodeName${PropertySelectorNode.token}$propName1${PropertySelectorNode.token}${PropertyAbbreviationSelectorNode.notOperator}$propName2"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = SelectorNode.Companion::parseForAddition)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack().dereference(analyzer.memory, toWrite = false) as? LxmNode
                ?: throw Error("The result must be a LxmNode")
        val props = result.getProperties(analyzer.memory, toWrite = false)
        Assertions.assertEquals(LxmLogic.True, props.getPropertyValue(analyzer.memory, propName1),
                "The property called $propName1 is incorrect")
        Assertions.assertEquals(LxmLogic.False, props.getPropertyValue(analyzer.memory, propName2),
                "The property called $propName2 is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @MethodSource("provideFilterOptions")
    fun `test filter`(isOk: Boolean, hasName: Boolean, hasProperty: Boolean, hasMethod: Boolean) {
        val nodeName = "nodeName"
        val propertyName = "propertyName"
        var grammar = if (hasName) {
            nodeName
        } else {
            ""
        }

        grammar += if (hasProperty) {
            "${PropertySelectorNode.token}$propertyName"
        } else {
            ""
        }

        grammar += if (hasMethod) {
            "${MethodSelectorNode.relationalToken}${AnalyzerCommons.SelectorMethods.Empty}"
        } else {
            ""
        }

        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = SelectorNode.Companion::parse)

        // Prepare stack.
        var lxmNode: LxmNode? = null
        if (isOk) {
            val lxmNode = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())

            lxmNode.getProperties(analyzer.memory, toWrite = true)
                    .setProperty(analyzer.memory, propertyName, LxmLogic.True)
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
        } else {
            when {
                hasMethod -> {
                    lxmNode = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
                    val lxmNodeAux = LxmNode(analyzer.memory, "aux", analyzer.text.saveCursor())
                    lxmNodeAux.addToParent(analyzer.memory, lxmNode)

                    lxmNode.getProperties(analyzer.memory, toWrite = true)
                            .setProperty(analyzer.memory, propertyName, LxmLogic.True)
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                }
                hasProperty -> {
                    val lxmNode = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())

                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                }
                else -> {
                    val lxmNode = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())

                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                }
            }
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(isOk), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        // Remove cyclic references.
        if (lxmNode != null) {
            lxmNode.getPrimitive().dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)
                    ?.clearChildren(analyzer.memory)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}

