package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.nodes.descriptive.statements.*
import org.lexem.angmar.compiler.functional.statements.*


/**
 * Analyzer for public macro statement.
 */
internal object PublicMacroStmtAnalyzer {
    const val signalEndElement = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: PublicMacroStmtCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.element)
            }
            signalEndElement -> {
                /** This node is executed in [VarDeclarationStmtAnalyzer] and [FunctionStmtAnalyzer] and [ExpressionStmtAnalyzer] and [FilterStmtAnalyzer] **/
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
