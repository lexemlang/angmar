package org.lexem.angmar.parser.functional.expressions

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for parenthesis expressions i.e. (expression).
 */
internal class ParenthesisExpressionNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    lateinit var expression: ParserNode

    override fun toString() = "$startToken$expression$endToken"

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("expression", expression.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ParenthesisExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val startToken = "("
        const val endToken = ")"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a parenthesis expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): ParenthesisExpressionNode? {
            val initCursor = parser.reader.saveCursor()
            val result = ParenthesisExpressionNode(parser, parent, parentSignal)

            if (!parser.readText(startToken)) {
                return null
            }

            WhitespaceNode.parse(parser)

            result.expression = ExpressionsCommons.parseExpression(parser, result,
                    ParenthesisExpressionAnalyzer.signalEndExpression) ?: throw AngmarParserException(
                    AngmarParserExceptionType.ParenthesisExpressionWithoutExpression,
                    "An expression was expected after the open parenthesis '$startToken'.") {
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

            WhitespaceNode.parse(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.ParenthesisExpressionWithoutEndToken,
                        "The close parenthesis was expected '$endToken'.") {
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
