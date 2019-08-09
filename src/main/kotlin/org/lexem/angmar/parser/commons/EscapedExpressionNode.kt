package org.lexem.angmar.parser.commons

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*

/**
 * Parser for escaped expression.
 */
class EscapedExpressionNode private constructor(parser: LexemParser, val expression: ParserNode) : ParserNode(parser) {

    override fun toString() = "$startToken$expression$endToken"


    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("expression", expression)

    }

    companion object {
        internal const val startToken = "\\("
        internal const val endToken = ")"

        // METHODS ------------------------------------------------------------

        /**
         * Parses an escaped expression.
         */
        fun parse(parser: LexemParser): EscapedExpressionNode? {
            parser.fromBuffer(parser.reader.currentPosition(), EscapedExpressionNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(startToken)) {
                return null
            }

            WhitespaceNoEOLNode.parse(parser)

            val expression = ExpressionsCommons.parseExpression(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.EscapedExpressionWithoutExpression,
                    "Escaped expression require an expression as its value.") {
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

            WhitespaceNoEOLNode.parse(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.EscapedExpressionWithoutEndToken,
                        "Escaped expression require the close parenthesis '$endToken'.") {
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

            val result = EscapedExpressionNode(parser, expression)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
