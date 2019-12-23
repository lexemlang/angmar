package org.lexem.angmar.compiler.others

import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.parser.*

/**
 * Compiled node that represents a no operation.
 */
internal class NoOperationCompiled(parent: CompiledNode?, parentSignal: Int, parserNode: ParserNode) :
        CompiledNode(parent, parentSignal, parserNode) {

    // Not do anything.
    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = analyzer.nextNode(parent, parentSignal)
}
