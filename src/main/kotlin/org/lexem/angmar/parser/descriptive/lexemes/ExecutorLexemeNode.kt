package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for executor lexemes.
 */
internal class ExecutorLexemeNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var isConditional = false
    val expressions = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        append(startToken)

        if (isConditional) {
            append(conditionalToken)
        }

        append(expressions.joinToString(", "))

        append(endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isConditional", isConditional)
        result.add("expressions", SerializationUtils.listToTest(expressions))

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ExecutorLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val startToken = "\\("
        const val endToken = ")"
        const val conditionalToken = "?"
        const val separatorToken = GlobalCommons.elementSeparator

        // METHODS ------------------------------------------------------------

        /**
         * Parses an executor lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): ExecutorLexemeNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ExecutorLexemeNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = ExecutorLexemeNode(parser, parent, parentSignal)

            if (!parser.readText(startToken)) {
                return null
            }

            result.isConditional = parser.readText(conditionalToken)

            WhitespaceNode.parse(parser)

            var prev = false
            while (true) {
                if (prev) {
                    if (!parser.readText(separatorToken)) {
                        break
                    }

                    WhitespaceNode.parse(parser)
                }

                val expression = ExpressionsCommons.parseExpression(parser, result,
                        result.expressions.size + ExecutorLexemAnalyzer.signalEndFirstExpression)
                if (expression == null) {
                    val expressionCursor = parser.reader.saveCursor()

                    // To show the end token in the message if it exists.
                    parser.readText(endToken)

                    if (!prev) {
                        throw AngmarParserException(AngmarParserExceptionType.ExecutorWithoutExpression,
                                "Executor lexemes require at least one expression.") {
                            val fullText = parser.reader.readAllText()
                            addSourceCode(fullText, parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(fullText, null) {
                                title = Consts.Logger.hintTitle
                                highlightCursorAt(expressionCursor.position())
                                message = "Try adding an expression here"
                            }
                        }
                    } else {
                        throw AngmarParserException(AngmarParserExceptionType.ExecutorWithoutExpressionAfterSeparator,
                                "Executor lexemes require an expression after the separator token '$separatorToken'.") {
                            val fullText = parser.reader.readAllText()
                            addSourceCode(fullText, parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(fullText, null) {
                                title = Consts.Logger.hintTitle
                                highlightSection(expressionCursor.position() - 1)
                                message = "Try removing the separator token '$separatorToken'"
                            }
                        }
                    }
                }

                WhitespaceNode.parse(parser)

                result.expressions.add(expression)
                prev = true
            }

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.ExecutorWithoutEndToken,
                        "Executor lexemes require the close parenthesis '$endToken'.") {
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
