package org.lexem.angmar.parser.functional.expressions.modifiers

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*

/**
 * Special node for calling internal functions. It cannot be parsed.
 */
internal object InternalFunctionCallNode : ParserNode(LexemParser(IOStringReader.from("")), null, 0) {
    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            InternalFunctionCallAnalyzer.stateMachine(analyzer, signal)

    override fun toTree(): JsonObject {
        throw AngmarUnreachableException()
    }

    override fun toString() = "[InternalFunctionCallNode]"
}
