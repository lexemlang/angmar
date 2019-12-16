package org.lexem.angmar.analyzer.nodes.descriptive.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.descriptive.statements.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Analyzer for expression statement.
 */
internal object ExpressionStmtAnalyzer {
    const val signalEndName = AnalyzerNodesCommons.signalStart + 1
    const val signalEndProperties = signalEndName + 1
    const val signalEndParameterList = signalEndProperties + 1
    const val signalEndBlock = signalEndParameterList + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ExpressionStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.name)
            }
            signalEndName -> {
                // Create the expression.
                val name = analyzer.memory.getLastFromStack() as LxmString
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
                val contextReference = AnalyzerCommons.getCurrentContextReference(analyzer.memory)
                val exp = LxmExpression(analyzer.memory, node, name.primitive, contextReference)
                val expRef = analyzer.memory.add(exp)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                // Add it to the exports if the parent is a public macro
                if (node.parent is PublicMacroStmtNode) {
                    val exports = context.getDereferencedProperty<LxmObject>(analyzer.memory,
                            AnalyzerCommons.Identifiers.Exports, toWrite = true)!!

                    exports.setProperty(analyzer.memory, name.primitive, expRef)
                }

                context.setProperty(analyzer.memory, name.primitive, expRef)
            }
            else -> {
                return AnalyzerNodesCommons.descriptiveExecutionController(analyzer, signal, node.properties,
                        node.parameterList, node.block, node, signalEndProperties, signalEndParameterList,
                        signalEndBlock)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
