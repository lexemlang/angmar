package org.lexem.angmar.nodes.expressions.modifiers

import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.commons.IdentifierNode


/**
 * Parser for access expressions i.e. element.access
 */
class AccessExpressionNode private constructor(parser: LexemParser, val identifier: IdentifierNode) :
        ParserNode(nodeType, parser) {

    override fun toString() = "$accessCharacter$identifier"

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("identifier", identifier)
    }

    companion object {
        private val nodeType = NodeType.AccessExpression
        const val accessCharacter = "."

        // METHODS ------------------------------------------------------------

        /**
         * Parses an access expression
         */
        fun parse(parser: LexemParser): AccessExpressionNode? {
            parser.fromBuffer<AccessExpressionNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            if (!parser.readText(accessCharacter)) {
                // It is not an error because statements can end with a '.'
                return null
            }

            val id = IdentifierNode.parse(parser)
            if (id == null) {
                initCursor.restore()
                return null
            }

            val result = AccessExpressionNode(parser, id)
            return parser.finalizeNode(result, initCursor)
        }
    }
}