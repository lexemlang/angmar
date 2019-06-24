package org.lexem.angmar.nodes.expressions

import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.commons.WhitespaceNoEOLNode
import org.lexem.angmar.nodes.expressions.operators.ExplicitPostfixOperatorsNode

/**
 * Parser for expression elements with postfix operators.
 */
class ExpressionElementWithPostfixNode private constructor(parser: LexemParser, val element: ParserNode) :
        ParserNode(nodeType, parser) {
    var postfixWhitespace: WhitespaceNoEOLNode? = null
    var postfixOperators: ExplicitPostfixOperatorsNode? = null
    var nextExpressionWhitespace: WhitespaceNoEOLNode? = null
    var nextExpression: ExpressionNode? = null

    override fun toString() = StringBuilder().apply {
        append(element)

        if (postfixOperators != null) {
            append(postfixWhitespace!!)
            append(postfixOperators)
        }

        if (nextExpression != null) {
            append(nextExpressionWhitespace!!)
            append(nextExpression)
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("element", element)

        if (postfixOperators != null) {
            printer.addField("postfixWhitespace", postfixWhitespace)
            printer.addField("postfixOperators", postfixOperators)
        }

        if (nextExpression != null) {
            printer.addField("nextExpressionWhitespace", nextExpressionWhitespace)
            printer.addField("nextExpression", nextExpression)
        }
    }

    companion object {
        private val nodeType = NodeType.ExpressionElementWithPostfix

        // METHODS ------------------------------------------------------------

        /**
         * Parse a expression element with postfix operators.
         */
        fun parse(parser: LexemParser): ExpressionElementWithPostfixNode? {
            parser.fromBuffer<ExpressionElementWithPostfixNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val element = ExpressionElementNode.parse(parser) ?: SemanticIdentifierListNode.parse(parser) ?: return null
            val result = ExpressionElementWithPostfixNode(parser, element)

            val prePostfixCursor = parser.reader.saveCursor()
            var whitespace = WhitespaceNoEOLNode.parseOrEmpty(parser)
            val postfix = ExplicitPostfixOperatorsNode.parse(parser)

            if (postfix != null) {
                result.postfixWhitespace = whitespace
                result.postfixOperators = postfix
            } else {
                prePostfixCursor.restore()
            }

            if (element is SemanticIdentifierListNode) {
                val preExpressionCursor = parser.reader.saveCursor()
                whitespace = WhitespaceNoEOLNode.parseOrEmpty(parser)
                val expression = ExpressionNode.parse(parser)

                if (postfix != null) {
                    result.nextExpressionWhitespace = whitespace
                    result.nextExpression = expression
                } else {
                    preExpressionCursor.restore()
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}