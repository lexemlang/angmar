package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.commons.*
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
                val accumulator = LxmString.from(node.texts[0])

                if (node.escapes.isNotEmpty()) {
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, accumulator)

                    return analyzer.nextNode(node.escapes[0])
                }

                analyzer.memory.addToStackAsLast(accumulator)
            }
            in signalEndFirstEscape..signalEndFirstEscape + node.escapes.size -> {
                val position = (signal - signalEndFirstEscape) + 1

                // Combine the strings depending on the type of escape.
                val accumulator = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as LxmString
                val concatenation = if (node.escapes[position - 1] is EscapedExpressionNode) {
                    val escape = analyzer.memory.getLastFromStack() as LxmString

                    accumulator.primitive + escape.primitive + node.texts[position]
                } else {
                    val escape = analyzer.memory.getLastFromStack() as LxmInteger

                    accumulator.primitive + Character.toChars(escape.primitive).joinToString("") + node.texts[position]
                }

                analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Accumulator, LxmString.from(concatenation))

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                // Evaluate the next operand.
                if (position < node.escapes.size) {
                    return analyzer.nextNode(node.escapes[position])
                }

                // Move Accumulator to Last in the stack.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
