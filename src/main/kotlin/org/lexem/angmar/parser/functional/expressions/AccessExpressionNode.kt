package org.lexem.angmar.parser.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Parser for accesses expression.
 */
class AccessExpressionNode private constructor(parser: LexemParser, var element: ParserNode) : ParserNode(parser) {
    var modifiers = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        append(element)
        modifiers.forEach { append(it) }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("element", element)
        printer.addField("modifiers", modifiers)
    }

    companion object {

        /**
         * Parses an access expression.
         */
        fun parse(parser: LexemParser): ParserNode? {
            parser.fromBuffer(parser.reader.currentPosition(), AccessExpressionNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            val element = ExpressionsCommons.parseLiteral(parser) ?: ExpressionsCommons.parseMacro(parser)
            ?: ParenthesisExpressionNode.parse(parser) ?: IdentifierNode.parse(parser) ?: return null

            val result = AccessExpressionNode(parser, element)

            while (true) {
                result.modifiers.add(
                        AccessExplicitMemberNode.parse(parser) ?: IndexerNode.parse(parser) ?: FunctionCallNode.parse(
                                parser) ?: break)
            }

            if (result.modifiers.isEmpty()) {
                return element
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
