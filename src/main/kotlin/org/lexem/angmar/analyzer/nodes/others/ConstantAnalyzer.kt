package org.lexem.angmar.analyzer.nodes.others

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.compiler.others.*


/**
 * Analyzer for nodes that holds a [LexemPrimitive]. Used to simplify an expression.
 */
internal object ConstantAnalyzer {
    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ConstantCompiled) {
        analyzer.memory.addToStackAsLast(node.value)

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
