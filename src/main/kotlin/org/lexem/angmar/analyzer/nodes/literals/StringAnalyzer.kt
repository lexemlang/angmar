package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*


/**
 * Analyzer for normal string literals.
 */
internal object StringAnalyzer {
    const val signalEndFirstElement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: StringCompiled) {
        val signalEndFirstCallToString = signalEndFirstElement + node.elements.size
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Add the first string.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, LxmString.Empty)

                return analyzer.nextNode(node.elements.first())
            }
            in signalEndFirstElement..signalEndFirstElement + node.elements.size -> {
                val position = (signal - signalEndFirstElement) + 1

                // Combine the strings or call the toString.
                val result = analyzer.memory.getLastFromStack()

                if (result !is LxmString) {
                    val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
                    val contextName = AnalyzerCommons.getContextName(analyzer.memory, context)
                    StdlibCommons.callToString(analyzer, result, node, signalEndFirstCallToString + position,
                            contextName.primitive)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    return
                }

                val accumulator = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as LxmString
                val concatenation = accumulator.primitive + result.primitive

                analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Accumulator, LxmString.from(concatenation))

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                // Evaluate the next operand.
                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }

                // Move Accumulator to Last in the stack.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
            in signalEndFirstCallToString..signalEndFirstCallToString + node.elements.size -> {
                val position = (signal - signalEndFirstCallToString) + 1

                // Combine the strings or call the toString.
                val result = analyzer.memory.getLastFromStack() as? LxmString ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.ToStringMethodNotReturningString,
                        "The ${AnalyzerCommons.Identifiers.ToString} method must always return a ${StringType.TypeName}") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(node.from.position(), node.to.position() - 1)
                    }
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.hintTitle
                        highlightSection(node.elements[position - 1].from.position(),
                                node.elements[position - 1].to.position() - 1)
                        message = "Review this expression"
                    }
                }

                val accumulator = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as LxmString
                val concatenation = accumulator.primitive + result.primitive

                analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Accumulator, LxmString.from(concatenation))

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                // Evaluate the next operand.
                if (position < node.elements.size) {
                    return analyzer.nextNode(node.elements[position])
                }

                // Move Accumulator to Last in the stack.
                analyzer.memory.renameStackCellToLast(AnalyzerCommons.Identifiers.Accumulator)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
