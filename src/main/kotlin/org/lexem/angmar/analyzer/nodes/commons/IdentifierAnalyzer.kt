package org.lexem.angmar.analyzer.nodes.commons

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.commons.*


/**
 * Analyzer for identifiers.
 */
internal object IdentifierAnalyzer {
    const val signalEndQuotedIdentifier = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: IdentifierNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.isQuotedIdentifier) {
                    return analyzer.nextNode(node.quotedIdentifier)
                }

                val value = node.simpleIdentifiers.joinToString(IdentifierNode.middleChar)
                analyzer.memory.addToStackAsLast(LxmString.from(value))
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
