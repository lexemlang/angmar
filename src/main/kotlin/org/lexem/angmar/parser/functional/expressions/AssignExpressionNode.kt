package org.lexem.angmar.parser.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for assign expressions.
 */
class AssignExpressionNode private constructor(parser: LexemParser, val left: ParserNode,
        val operator: AssignOperatorNode, val right: ParserNode) : ParserNode(parser) {

    override fun toString() = StringBuilder().apply {
        append(left)
        append(' ')
        append(operator)
        append(' ')
        append(right)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("left", left)
        printer.addField("operator", operator)
        printer.addField("right", right)
    }

    companion object {

        /**
         * Parses an assign expression.
         */
        fun parse(parser: LexemParser): AssignExpressionNode? {
            parser.fromBuffer(parser.reader.currentPosition(), AssignExpressionNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            val left = ExpressionsCommons.parseLeftExpression(parser) ?: return null

            WhitespaceNoEOLNode.parse(parser)

            val operator = AssignOperatorNode.parse(parser) ?: let {
                initCursor.restore()
                return@parse null
            }

            WhitespaceNode.parse(parser)

            val right = ExpressionsCommons.parseRightExpression(parser) ?: let {
                throw AngmarParserException(
                        AngmarParserExceptionType.AssignExpressionWithoutExpressionAfterAssignOperator,
                        "An expression was expected after the assign operator '$operator'.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightSection(operator.from.position(), operator.to.position())
                        message("Try removing the assign operator")
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(operator.to.position())
                        message("Try adding an expression after the operator")
                    }
                }
            }

            val result = AssignExpressionNode(parser, left, operator, right)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
