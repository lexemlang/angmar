package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.descriptive.lexemes.*


/**
 * Analyzer for group header lexemes.
 */
internal object GroupHeaderLexemAnalyzer {
    const val signalEndQuantifier = AnalyzerNodesCommons.signalStart + 1
    const val signalEndIdentifier = signalEndQuantifier + 1
    const val signalEndPropertyBlock = signalEndIdentifier + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: GroupHeaderLexemeCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.quantifier != null) {
                    return analyzer.nextNode(node.quantifier)
                } else {
                    val patternUnion =
                            LxmPatternUnion(analyzer.memory, LxmQuantifier.AlternativePattern, LxmInteger.Num0)
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LexemeUnion, patternUnion)
                }

                if (node.identifier != null) {
                    return analyzer.nextNode(node.identifier)
                } else {
                    createNode(analyzer, "", node.isFilterCode)
                }

                if (node.propertyBlock != null) {
                    return analyzer.nextNode(node.propertyBlock)
                }
            }
            signalEndQuantifier -> {
                val quantifier = analyzer.memory.getLastFromStack() as LxmQuantifier
                val patternUnion = LxmPatternUnion(analyzer.memory, quantifier, LxmInteger.Num0)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LexemeUnion, patternUnion)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (node.identifier != null) {
                    return analyzer.nextNode(node.identifier)
                } else {
                    createNode(analyzer, "", node.isFilterCode)
                }

                if (node.propertyBlock != null) {
                    return analyzer.nextNode(node.propertyBlock)
                }
            }
            signalEndIdentifier -> {
                val name = analyzer.memory.getLastFromStack() as LxmString

                createNode(analyzer, name.primitive, node.isFilterCode)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (node.propertyBlock != null) {
                    return analyzer.nextNode(node.propertyBlock)
                }
            }
            signalEndPropertyBlock -> {
                val values =
                        analyzer.memory.getLastFromStack().dereference(analyzer.memory, toWrite = false) as LxmObject
                val properties = AnalyzerCommons.getCurrentNodeProps(analyzer.memory, toWrite = true)

                for ((key, value) in values.getAllIterableProperties()) {
                    properties.setProperty( key, value.value)
                }

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Creates a new node by the specified name.
     */
    fun createNode(analyzer: LexemAnalyzer, name: String, isFilterCode: Boolean) {
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)

        val type = if (isFilterCode) {
            LxmNode.LxmNodeType.FilterGroup
        } else {
            LxmNode.LxmNodeType.ExpressionGroup
        }

        val lxmNode = analyzer.createNewNode(name, type)

        context.setProperty( AnalyzerCommons.Identifiers.Node, lxmNode, isConstant = true)
    }
}
