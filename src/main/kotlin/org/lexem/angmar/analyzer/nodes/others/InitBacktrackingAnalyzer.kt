package org.lexem.angmar.analyzer.nodes.others

import org.lexem.angmar.*
import org.lexem.angmar.compiler.others.*


/**
 * Parser node that inits the backtracking. Used to simplify an expression.
 */
internal object InitBacktrackingAnalyzer {
    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: InitBacktrackingCompiled) {
        analyzer.backtrackingData = null
        return analyzer.initBacktracking()
    }
}
