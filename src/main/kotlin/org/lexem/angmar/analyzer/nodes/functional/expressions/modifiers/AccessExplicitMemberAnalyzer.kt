package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
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
                return analyzer.nextNode(node.identifier)
            }
            signalEndIdentifier -> {
                val identifier = analyzer.memory.popStack() as LxmString
                val element = (analyzer.memory.popStack() as LexemSetter).resolve(analyzer.memory)

                analyzer.memory.pushStack(LxmPropertySetter(element, identifier.primitive, node, analyzer.memory))
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
