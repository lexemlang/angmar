package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.primitives.setters.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Analyzer for access explicit member.
 */
internal object AccessExplicitMemberAnalyzer {
    const val signalEndIdentifier = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: AccessExplicitMemberNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Move Last to Accumulator in stack.
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Accumulator)

                return analyzer.nextNode(node.identifier)
            }
            signalEndIdentifier -> {
                val identifier = analyzer.memory.getLastFromStack() as LxmString
                val element = AnalyzerNodesCommons.resolveSetter(analyzer.memory,
                        analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator))

                analyzer.memory.replaceLastStackCell(
                        LxmPropertySetter(element, identifier.primitive, node, analyzer.memory))

                // Remove Accumulator from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
