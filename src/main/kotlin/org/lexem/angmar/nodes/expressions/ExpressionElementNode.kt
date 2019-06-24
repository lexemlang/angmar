package org.lexem.angmar.nodes.expressions

import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.commons.IdentifierNode
import org.lexem.angmar.nodes.commons.WhitespaceNode
import org.lexem.angmar.nodes.expressions.modifiers.AccessExpressionNode

/**
 * Parser for expression elements.
 */
class ExpressionElementNode private constructor(parser: LexemParser, val element: ParserNode) :
        ParserNode(nodeType, parser) {
    val whitespaces = mutableListOf<ParserNode>()
    val modifiers = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        append(element)
        for (i in whitespaces.zip(modifiers)) {
            append(i.first)
            append(i.second)
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("element", element)
        printer.addField("whitespaces", whitespaces)
        printer.addField("modifiers", modifiers)
    }

    companion object {
        private val nodeType = NodeType.ExpressionElement

        // METHODS ------------------------------------------------------------

        /**
         * Parses an expression element.
         */
        fun parse(parser: LexemParser): ExpressionElementNode? {
            parser.fromBuffer<ExpressionElementNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val element = ExpressionsCommons.parseLiteral(parser) ?: IdentifierNode.parse(parser) ?: return null
            val result = ExpressionElementNode(parser, element)

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()
                val whitespace = WhitespaceNode.parseOrEmpty(parser)
                val modifier = AccessExpressionNode.parse(parser)
                if (modifier == null) {
                    initLoopCursor.restore()
                    break
                }

                result.whitespaces.add(whitespace)
                result.modifiers.add(modifier)
            }

            if (element is IdentifierNode && result.modifiers.isEmpty()) {
                initCursor.restore()
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}