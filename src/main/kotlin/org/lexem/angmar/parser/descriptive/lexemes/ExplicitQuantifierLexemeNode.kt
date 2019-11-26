package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for explicit quantifier lexemes.
 */
internal class ExplicitQuantifierLexemeNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    var hasMaximum: Boolean = false
    var maximum: ParserNode? = null
    lateinit var minimum: ParserNode


    override fun toString() = StringBuilder().apply {
        append(startToken)

        append(minimum)

        if (hasMaximum) {
            append(elementSeparator)
            append(' ')

            if (maximum != null) {
                append(maximum!!)
            }
        }

        append(endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("minimum", minimum.toTree())
        result.addProperty("hasMaximum", hasMaximum)
        result.add("maximum", maximum?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ExplicitQuantifierLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val startToken = "{"
        const val endToken = "}"
        const val elementSeparator = GlobalCommons.elementSeparator

        // METHODS ------------------------------------------------------------

        /**
         * Parses an explicit quantifier lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): ExplicitQuantifierLexemeNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ExplicitQuantifierLexemeNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = ExplicitQuantifierLexemeNode(parser, parent, parentSignal)

            if (!parser.readText(startToken)) {
                return null
            }

            WhitespaceNode.parse(parser)

            result.minimum = ExpressionsCommons.parseExpression(parser, result,
                    ExplicitQuantifierLexemeAnalyzer.signalEndMinimum) ?: let {
                val expressionCursor = parser.reader.saveCursor()

                // To show the end token in the message if it exists.
                parser.readText(endToken)

                throw AngmarParserException(AngmarParserExceptionType.QuantifierWithoutMinimumExpression,
                        "Quantifiers require an expression to act as the minimum value after the open bracket '$startToken'.") {
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
            }

            WhitespaceNode.parse(parser)

            if (parser.readText(elementSeparator)) {
                result.hasMaximum = true

                WhitespaceNode.parse(parser)

                result.maximum = ExpressionsCommons.parseExpression(parser, result,
                        ExplicitQuantifierLexemeAnalyzer.signalEndMaximum)

                if (result.maximum != null) {
                    WhitespaceNode.parse(parser)
                }
            }

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.QuantifierWithoutEndToken,
                        "Quantifiers require the close bracket '$endToken'.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the close bracket '$endToken' here"
                    }
                }

            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
