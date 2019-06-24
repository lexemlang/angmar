package org.lexem.angmar.nodes.expressions.operators

import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.commons.IdentifierNode

/**
 * Parser for infix operators.
 */
class InfixOperatorNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
    var graphic: GraphicOperatorNode? = null
    var identifier: IdentifierNode? = null

    val isGraphic get() = graphic != null

    override fun toString() = if (isGraphic) {
        graphic.toString()
    } else {
        identifier.toString()
    }

    override fun toTree(printer: TreeLikePrinter) {
        if (graphic != null) {
            printer.addField("graphic", graphic)
        } else {
            printer.addField("identifier", identifier)
        }
    }

    companion object {
        private val nodeType = NodeType.InfixOperator

        // METHODS ------------------------------------------------------------

        /**
         * Parses an infix operator.
         */
        fun parse(parser: LexemParser): InfixOperatorNode? {
            parser.fromBuffer<InfixOperatorNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = InfixOperatorNode(parser)

            val graphic = GraphicOperatorNode.parse(parser)
            if (graphic != null) {
                result.graphic = graphic
            } else {
                result.identifier = IdentifierNode.parse(parser) ?: return null
            }

            // It cannot end with a [GraphicOperatorNode.operatorSeparator] because it will be a prefix.
            if (parser.readText(GraphicOperatorNode.operatorSeparator)) {
                initCursor.restore()
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}