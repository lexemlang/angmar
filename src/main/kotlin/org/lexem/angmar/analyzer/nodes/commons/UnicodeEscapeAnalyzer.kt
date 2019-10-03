package org.lexem.angmar.analyzer.nodes.commons

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.commons.*


/**
 * Analyzer for unicode escapes.
 */
internal object UnicodeEscapeAnalyzer {
    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: UnicodeEscapeNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                val prevValue = analyzer.memory.popStack() as LxmString
                val byteArray = ByteArray(4)
                byteArray[1] = node.value.shr(16).and(0xFF).toByte()
                byteArray[2] = node.value.shr(8).and(0xFF).toByte()
                byteArray[3] = node.value.and(0xFF).toByte()

                val decodedPoint = byteArray.toString(Charsets.UTF_32)

                analyzer.memory.pushStack(LxmString.from(prevValue.primitive + decodedPoint))
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
