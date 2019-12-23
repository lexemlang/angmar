package org.lexem.angmar.parser.commons

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*

/**
 * Parser for escaped expressions.
 */
internal class EscapedExpressionNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var expression: ParserNode

    override fun toString() = "$startToken$expression$endToken"

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("expression", expression.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) = expression.compile(parent, parentSignal)

    companion object {
        internal const val startToken = "\\("
        internal const val endToken = ")"

        // METHODS ------------------------------------------------------------

        /**
         * Parses an escaped expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode): EscapedExpressionNode? {
            val initCursor = parser.reader.saveCursor()
            val result = EscapedExpressionNode(parser, parent)

            if (!parser.readText(startToken)) {
                return null
            }

            WhitespaceNoEOLNode.parse(parser)

            result.expression = ExpressionsCommons.parseExpression(parser, result) ?: throw AngmarParserException(
                    AngmarParserExceptionType.EscapedExpressionWithoutExpression,
                    "Escaped expression require an expression as its value.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightCursorAt(parser.reader.currentPosition())
                    message = "Try adding an expression here"
                }
            }

            WhitespaceNoEOLNode.parse(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.EscapedExpressionWithoutEndToken,
                        "Escaped expression require the close parenthesis '$endToken'.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the close parenthesis '$endToken' here"
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
