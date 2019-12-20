package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*

/**
 * Parser for logical values.
 */
internal class LogicNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var value: Boolean = false

    override fun toString() = if (value) {
        trueLiteral
    } else {
        falseLiteral
    }

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("value", value)

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = LogicAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val trueLiteral = "true"
        const val falseLiteral = "false"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a logical value.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): LogicNode? {
            val initCursor = parser.reader.saveCursor()
            val result = when {
                Commons.parseKeyword(parser, trueLiteral) -> {
                    val res = LogicNode(parser, parent, parentSignal)
                    res.value = true
                    res
                }
                Commons.parseKeyword(parser, falseLiteral) -> {
                    LogicNode(parser, parent, parentSignal)
                }
                else -> return null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
