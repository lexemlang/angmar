package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Analyzer for function statement.
 */
internal object FunctionStmtAnalyzer {
    const val signalEndName = AnalyzerNodesCommons.signalStart + 1
    const val signalEndParameterList = signalEndName + 1
    const val signalEndBlock = signalEndParameterList + 1

    // METHODS ----------------------------------------------------------c------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: FunctionStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.name)
            }
            signalEndName -> {
                // Create the function.
                val name = analyzer.memory.getLastFromStack() as LxmString
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                val contextReference = AnalyzerCommons.getCurrentContextReference(analyzer.memory)
                val fn = LxmFunction(analyzer.memory, node, contextReference)
                fn.name = name.primitive
                val fnRef = analyzer.memory.add(fn)

                // Add it to the exports if the parent is a public macro
                if (node.parent is PublicMacroStmtNode) {
                    val exports = context.getDereferencedProperty<LxmObject>(analyzer.memory,
                            AnalyzerCommons.Identifiers.Exports)!!

                    exports.setProperty(analyzer.memory, name.primitive, fnRef)
                }

                context.setProperty(analyzer.memory, name.primitive, fnRef)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()
            }
            else -> {
                return AnalyzerNodesCommons.functionExecutionController(analyzer, signal, node.parameterList,
                        node.block, node, signalEndParameterList, signalEndBlock)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
