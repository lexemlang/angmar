package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for Nil literals.
 */
internal object NilAnalyzer {
    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: NilNode) {
        analyzer.memory.pushStack(LxmNil)

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
