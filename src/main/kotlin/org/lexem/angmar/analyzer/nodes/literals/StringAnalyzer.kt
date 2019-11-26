package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for normal string literals.
 */
internal object StringAnalyzer {
    const val signalEndFirstEscape = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: StringNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Add the first string.
                analyzer.memory.addToStackAsLast(LxmString.from(node.texts[0]))

                if (node.escapes.isNotEmpty()) {
                    return analyzer.nextNode(node.escapes[0])
                }
            }
            in signalEndFirstEscape..signalEndFirstEscape + node.escapes.size -> {
                val position = (signal - signalEndFirstEscape) + 1

                // Combine the strings.
                val value = analyzer.memory.getLastFromStack() as LxmString
                val result = value.primitive + node.texts[position]

                analyzer.memory.replaceLastStackCell(LxmString.from(result))

                // Evaluate the next operand.
                if (position < node.escapes.size) {
                    return analyzer.nextNode(node.escapes[position])
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
