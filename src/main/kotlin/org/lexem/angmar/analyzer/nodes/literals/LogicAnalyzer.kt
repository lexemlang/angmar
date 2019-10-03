package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for Logic literals.
 */
internal object LogicAnalyzer {
    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: LogicNode) {
        analyzer.memory.pushStack(LxmLogic.from(node.value))

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
