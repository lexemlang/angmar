package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*

/**
 * Parser for logical values.
 */
class LogicNode private constructor(parser: LexemParser, val value: Boolean) : ParserNode(parser) {
    override fun toString() = if (value) {
        trueLiteral
    } else {
        falseLiteral
    }

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("value", value)
    }

    companion object {
        const val trueLiteral = "true"
        const val falseLiteral = "false"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a logical value.
         */
        fun parse(parser: LexemParser): LogicNode? {
            val initCursor = parser.reader.saveCursor()
            val result = when {
                Commons.parseKeyword(parser, trueLiteral) -> {
                    LogicNode(parser, true)
                }
                Commons.parseKeyword(parser, falseLiteral) -> {
                    LogicNode(parser, false)
                }
                else -> return null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
