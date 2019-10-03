package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for nil/null values.
 */
internal class NilNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    override fun toString() = nilLiteral

    override fun toTree(): JsonObject {
        return super.toTree()
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = NilAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val nilLiteral = "nil"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a nil value.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): NilNode? {
            parser.fromBuffer(parser.reader.currentPosition(), NilNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = if (Commons.parseKeyword(parser, nilLiteral)) {
                NilNode(parser, parent, parentSignal)
            } else {
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
