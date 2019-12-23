package org.lexem.angmar.analyzer.nodes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*


/**
 * Analyzer for Lexem files.
 */
internal object LexemFileAnalyzer {
    const val signalEndFirstStatement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: LexemFileCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Generate a new module context.
                AnalyzerCommons.createAndAssignNewModuleContext(analyzer, node.parser.reader.getSource())

                if (node.statements.isNotEmpty()) {
                    return analyzer.nextNode(node.statements.first())
                }

                return finalizeFile(analyzer, node)
            }
            in signalEndFirstStatement..signalEndFirstStatement + node.statements.size -> {
                val position = (signal - signalEndFirstStatement) + 1

                // Process the next node.
                if (position < node.statements.size) {
                    return analyzer.nextNode(node.statements[position])
                }

                return finalizeFile(analyzer, node)
            }

            // Throw an error.
            AnalyzerNodesCommons.signalReturnControl, AnalyzerNodesCommons.signalExitControl, AnalyzerNodesCommons.signalNextControl, AnalyzerNodesCommons.signalRedoControl, AnalyzerNodesCommons.signalRestartControl -> {
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl

                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.UnhandledControlStatementSignal,
                        "The ${control.type} control signal has not reached any valid statement.") {
                    val fullText = node.parserNode!!.parser.reader.readAllText()
                    addSourceCode(fullText, node.parserNode!!.parser.reader.getSource()) {
                        title = Consts.Logger.hintTitle
                        highlightSection(control.node.from.position(), control.node.to.position() - 1)
                        message = "Review that this control statement has a matching statement."
                    }

                    if (control.tag != null) {
                        addNote(Consts.Logger.hintTitle, "Check that any statement has the tag: ${control.tag}")

                        val name = AnalyzerCommons.getCurrentContextTag(analyzer.memory)
                        if (name != null && name == control.tag) {
                            addNote(Consts.Logger.hintTitle,
                                    "A tag in the block of a function cannot receive the ${control.type} control signal")
                        }
                    }
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Finalizes the file node.
     */
    private fun finalizeFile(analyzer: LexemAnalyzer, node: LexemFileCompiled) {
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val exports = context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Exports) as LxmReference

        analyzer.memory.addToStackAsLast(exports)

        // Remove the context.
        AnalyzerCommons.removeModuleContextAndAssignPrevious(analyzer.memory)

        // Recover the last module code point.
        val lastModuleCodePoint =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint) as? LxmCodePoint

        // Remove HiddenLastModuleCodePoint from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint)

        return if (lastModuleCodePoint != null) {
            analyzer.nextNode(lastModuleCodePoint)
        } else {
            // Root file.
            analyzer.nextNode(node.parent, node.parentSignal)

            // Remove Last from the stack.
            analyzer.memory.removeLastFromStack()
        }
    }
}
