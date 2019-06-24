package org.lexem.angmar.nodes.literals

import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.commons.IdentifierNode

/**
 * Parser for boolean values.
 */
class BooleanNode private constructor(parser: LexemParser, val value: Boolean) : ParserNode(nodeType, parser) {
    override fun toString() = if (value) {
        trueLiteral
    } else {
        falseLiteral
    }

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("value", value)
    }

    companion object {
        private val nodeType = NodeType.Boolean
        const val trueLiteral = "true"
        const val falseLiteral = "false"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a boolean value.
         */
        fun parse(parser: LexemParser): BooleanNode? {
            val initCursor = parser.reader.saveCursor()
            val result = when {
                IdentifierNode.parseKeyword(parser, trueLiteral) -> {
                    BooleanNode(parser, true)
                }
                IdentifierNode.parseKeyword(parser, falseLiteral) -> {
                    BooleanNode(parser, false)
                }
                else -> return null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}