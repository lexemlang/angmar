package org.lexem.angmar.parser.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for parenthesis expressions i.e. (expression).
 */
class ParenthesisExpressionNode private constructor(parser: LexemParser, val expression: ParserNode) :
        ParserNode(parser) {

    override fun toString() = "$startToken$expression$endToken"

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("expression", expression)
    }

    companion object {
        const val startToken = "("
        const val endToken = ")"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a parenthesis expression.
         */
        fun parse(parser: LexemParser): ParenthesisExpressionNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ParenthesisExpressionNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            if (!parser.readText(startToken)) {
                return null
            }

            WhitespaceNode.parse(parser)

            val expression = ExpressionsCommons.parseExpression(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.ParenthesisExpressionWithoutExpression,
                    "An expression was expected after the open parenthesis '$startToken'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an expression here")
                }
            }

            WhitespaceNode.parse(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.ParenthesisExpressionWithoutEndToken,
                        "The close parenthesis was expected '$endToken'.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(parser.reader.currentPosition())
                        message("Try adding the close parenthesis '$endToken' here")
                    }
                }

            }

            val result = ParenthesisExpressionNode(parser, expression)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
