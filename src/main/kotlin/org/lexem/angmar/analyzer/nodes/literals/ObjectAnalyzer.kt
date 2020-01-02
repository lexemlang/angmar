package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.literals.*


/**
 * Analyzer for object literals.
 */
internal object ObjectAnalyzer {
    const val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ObjectCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Add the new object.
                val obj = LxmObject(analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, obj)

                if (node.elements.isNotEmpty()) {
                    return analyzer.nextNode(node.elements.first())
                }

                if (node.isConstant) {
                    obj.makeConstantAndNotWritable(analyzer.memory)
                }

                // Move accumulator to last.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
            in signalEndFirstElement..signalEndFirstElement + node.elements.size -> {
                val position = (signal - signalEndFirstElement) + 1

                // Process the next node.
                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }

                if (node.isConstant) {
                    val obj = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                            analyzer.memory, toWrite = true) as LxmObject

                    obj.makeConstantAndNotWritable(analyzer.memory)
                }

                // Move Accumulator to last.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
