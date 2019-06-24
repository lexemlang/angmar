package org.lexem.angmar.nodes.literals

import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.commons.IdentifierNode


/**
 * Parser for nil/null values.
 */
class NilNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
    override fun toString() = nilLiteral

    override fun toTree(printer: TreeLikePrinter) {
    }

    companion object {
        private val nodeType = NodeType.Nil
        const val nilLiteral = "nil"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a nil value.
         */
        fun parse(parser: LexemParser): NilNode? {
            val initCursor = parser.reader.saveCursor()
            val result = if (IdentifierNode.parseKeyword(parser, nilLiteral)) {
                NilNode(parser)
            } else {
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}