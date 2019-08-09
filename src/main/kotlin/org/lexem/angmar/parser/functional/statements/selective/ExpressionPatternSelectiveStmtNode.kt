package org.lexem.angmar.parser.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for expression patterns of the selective statements.
 */
class ExpressionPatternSelectiveStmtNode private constructor(parser: LexemParser, val expression: ParserNode) :
        ParserNode(parser) {
    var conditional: ConditionalPatternSelectiveStmtNode? = null

    override fun toString() = StringBuilder().apply {
        append(expression)
        if (conditional != null) {
            append(' ')
            append(conditional)
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("expression", expression)
        printer.addOptionalField("conditional", conditional)
    }

    companion object {
        /**
         * Parses a expression pattern of the selective statements.
         */
        fun parse(parser: LexemParser): ExpressionPatternSelectiveStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ExpressionPatternSelectiveStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            val expression = ExpressionsCommons.parseExpression(parser) ?: return null

            val result = ExpressionPatternSelectiveStmtNode(parser, expression)

            // conditional
            let {
                val initConditionalCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                result.conditional = ConditionalPatternSelectiveStmtNode.parse(parser)
                if (result.conditional == null) {
                    initConditionalCursor.restore()
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
