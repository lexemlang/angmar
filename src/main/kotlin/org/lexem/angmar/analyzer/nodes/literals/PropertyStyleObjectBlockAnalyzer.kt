package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for property-style object blocks.
 */
internal object PropertyStyleObjectBlockAnalyzer {
    const val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: PropertyStyleObjectBlockNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Create object
                val arguments = LxmObject()
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, analyzer.memory.add(arguments))

                // Call next element
                if (node.positiveElements.isNotEmpty()) {
                    return analyzer.nextNode(node.positiveElements[0])
                }

                if (node.negativeElements.isNotEmpty()) {
                    return analyzer.nextNode(node.negativeElements[0])
                }

                if (node.setElements.isNotEmpty()) {
                    return analyzer.nextNode(node.setElements[0])
                }

                // Move accumulator to last.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
            // Positives
            in signalEndFirstElement until signalEndFirstElement + node.positiveElements.size -> {
                val position = (signal - signalEndFirstElement) + 1

                val identifier = analyzer.memory.getLastFromStack()

                if (identifier !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                            "The returned value by the expression must be a ${StringType.TypeName}. Actual value: $identifier") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.positiveElements[position - 1].from.position(),
                                    node.positiveElements[position - 1].to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                val obj = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                        analyzer.memory) as LxmObject

                obj.setProperty(analyzer.memory, identifier.primitive, LxmLogic.True)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                // Call next element
                if (position < node.positiveElements.size) {
                    return analyzer.nextNode(node.positiveElements[position])
                }

                if (node.negativeElements.isNotEmpty()) {
                    return analyzer.nextNode(node.negativeElements[0])
                }

                if (node.setElements.isNotEmpty()) {
                    return analyzer.nextNode(node.setElements[0])
                }

                // Move accumulator to last.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
            // Negatives
            in signalEndFirstElement + node.positiveElements.size until signalEndFirstElement + node.positiveElements.size + node.negativeElements.size -> {
                val position = (signal - (signalEndFirstElement + node.positiveElements.size)) + 1

                val identifier = analyzer.memory.getLastFromStack()

                if (identifier !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                            "The returned value by the expression must be a ${StringType.TypeName}. Actual value: $identifier") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.negativeElements[position - 1].from.position(),
                                    node.negativeElements[position - 1].to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                val obj = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                        analyzer.memory) as LxmObject

                obj.setProperty(analyzer.memory, identifier.primitive, LxmLogic.False)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                // Call next element
                if (position < node.negativeElements.size) {
                    return analyzer.nextNode(node.negativeElements[position])
                }

                if (node.setElements.isNotEmpty()) {
                    return analyzer.nextNode(node.setElements[0])
                }

                // Move accumulator to last.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
            // Sets
            in signalEndFirstElement + node.positiveElements.size + node.negativeElements.size until signalEndFirstElement + node.positiveElements.size + node.negativeElements.size + node.setElements.size -> {
                val position =
                        (signal - (signalEndFirstElement + node.positiveElements.size + node.negativeElements.size)) + 1

                // Call next element
                if (position < node.setElements.size) {
                    return analyzer.nextNode(node.setElements[position])
                }

                // Move accumulator to last.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
