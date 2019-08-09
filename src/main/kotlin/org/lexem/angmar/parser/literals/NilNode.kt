package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for nil/null values.
 */
class NilNode private constructor(parser: LexemParser) : ParserNode(parser) {
    override fun toString() = nilLiteral

    override fun toTree(printer: TreeLikePrinter) {
    }

    companion object {
        const val nilLiteral = "nil"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a nil value.
         */
        fun parse(parser: LexemParser): NilNode? {
            val initCursor = parser.reader.saveCursor()
            val result = if (Commons.parseKeyword(parser, nilLiteral)) {
                NilNode(parser)
            } else {
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
