package org.lexem.angmar.analyzer.nodes.descriptive.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.descriptive.statements.*
import org.lexem.angmar.compiler.functional.statements.*


/**
 * Analyzer for expression statement.
 */
internal object ExpressionStmtAnalyzer {
    const val signalEndName = AnalyzerNodesCommons.signalStart + 1
    const val signalEndProperties = signalEndName + 1
    const val signalEndParameterList = signalEndProperties + 1
    const val signalEndBlock = signalEndParameterList + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ExpressionStmtCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.name)
            }
            signalEndName -> {
                // Create the expression.
                val name = analyzer.memory.getLastFromStack() as LxmString
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
                val exp = LxmExpression(analyzer.memory, node, name.primitive, context)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                // Add it to the exports if the parent is a public macro
                if (node.parent is PublicMacroStmtCompiled) {
                    val exports = context.getDereferencedProperty<LxmObject>(AnalyzerCommons.Identifiers.Exports,
                            toWrite = true)!!

                    exports.setProperty(name.primitive, exp)
                }

                context.setProperty(name.primitive, exp)
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
