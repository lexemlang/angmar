package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.lexemes.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for quantified block modifier.
 */
internal class QuantifiedGroupModifierNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    var minimum: ParserNode? = null
    var hasMaximum: Boolean = false
    var maximum: ParserNode? = null

    override fun toString() = StringBuilder().apply {
        append(startToken)

        if (minimum != null) {
            append(minimum!!)
        }

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

        result.add("minimum", minimum?.toTree())
        result.addProperty("hasMaximum", hasMaximum)
        result.add("maximum", maximum?.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            QuantifiedGroupModifierCompiled.compile(parent, parentSignal, this)

    companion object {
        const val startToken = ExplicitQuantifierLexemeNode.startToken
        const val endToken = ExplicitQuantifierLexemeNode.endToken
        const val elementSeparator = ExplicitQuantifierLexemeNode.elementSeparator

        // METHODS ------------------------------------------------------------

        /**
         * Parses a quantified block modifier.
         */
        fun parse(parser: LexemParser, parent: ParserNode): QuantifiedGroupModifierNode? {
            val initCursor = parser.reader.saveCursor()
            val result = QuantifiedGroupModifierNode(parser, parent)

            if (!parser.readText(startToken)) {
                return null
            }

            WhitespaceNode.parse(parser)

            result.minimum = ExpressionsCommons.parseExpression(parser, result)

            if (result.minimum != null) {
                WhitespaceNode.parse(parser)
            }

            if (parser.readText(elementSeparator)) {
                val postElementCursor = parser.reader.saveCursor()

                result.hasMaximum = true

                WhitespaceNode.parse(parser)

                result.maximum = ExpressionsCommons.parseExpression(parser, result)

                if (result.minimum == null && result.maximum == null) {
                    WhitespaceNode.parse(parser)
                    parser.readText(endToken)

                    throw AngmarParserException(AngmarParserExceptionType.QuantifierWithoutMaximumValue,
                            "Quantifiers require an expression after the separator '$elementSeparator'.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(postElementCursor.position() - elementSeparator.length,
                                    postElementCursor.position() - 1)
                            message = "Try removing the separator '$elementSeparator' here"
                        }
                    }
                }

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
