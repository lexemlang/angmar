package org.lexem.angmar.compiler.others

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.others.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.*

/**
 * Compiled node that inits the backtracking. Used to simplify an expression.
 */
internal class InitBacktrackingCompiled(parent: CompiledNode?, parentSignal: Int, parserNode: ParserNode) :
        CompiledNode(parent, parentSignal, parserNode) {

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            InitBacktrackingAnalyzer.stateMachine(analyzer, signal, this)
}
