package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*


/**
 * Analyzer for Bitlist literals.
 */
internal object BitListAnalyzer {
    const val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: BitlistCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Empty bitlist.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmBitList.Empty)

                return analyzer.nextNode(node.elements.first())
            }
            in signalEndFirstElement..signalEndFirstElement + node.elements.size -> {
                val position = (signal - signalEndFirstElement) + 1

                // Add the values.
                val accumulator = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as LxmBitList
                val right = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.IncompatibleType,
                        "${BitListType.TypeName} literals require that internal escaped expressions return a ${BitListType.TypeName}.") {
                    val expression = node.elements[position - 1]
                    val fullText = expression.parser.reader.readAllText()
                    addSourceCode(fullText, expression.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(expression.from.position(), expression.to.position() - 1)
                        addNote(Consts.Logger.hintTitle,
                                "The value returned by this expression must be a ${BitListType.TypeName}")
                    }
                }

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                val newAccumulator = LxmBitList(BitlistCompiled.addTwoBitLists(accumulator.primitive, right.primitive))
                analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Accumulator, newAccumulator)

                // Evaluate the next operand if it exist.
                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }

                // Move accumulator to last.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
