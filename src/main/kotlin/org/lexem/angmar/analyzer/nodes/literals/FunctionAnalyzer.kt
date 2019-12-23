package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.literals.*


/**
 * Analyzer for function literals.
 */
internal object FunctionAnalyzer {
    const val signalEndParameterList = AnalyzerNodesCommons.signalStart + 1
    const val signalEndBlock = signalEndParameterList + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: FunctionCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Create the function.
                val ctx = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
                val fn = LxmFunction(analyzer.memory, node, ctx)

                analyzer.memory.addToStackAsLast(fn)
            }
            else -> {
                return AnalyzerNodesCommons.functionExecutionController(analyzer, signal, node.parameterList,
                        node.block, node, signalEndParameterList, signalEndBlock)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
