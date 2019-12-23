package org.lexem.angmar.parser.descriptive.lexemes.anchors

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.lexemes.anchors.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.selectors.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for absolute anchor lexemes.
 */
internal class AbsoluteAnchorLexemeNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    var isNegated = false
    var elements = mutableListOf<AbsoluteElementAnchorLexemeNode>()

    override fun toString() = StringBuilder().apply {
        if (isNegated) {
            append(notOperator)
        }

        append(startToken)
        append(elements.joinToString(" "))
        append(endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isNegated", isNegated)
        result.add("elements", SerializationUtils.listToTest(elements))

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            AbsoluteAnchorLexemeCompiled.compile(parent, parentSignal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator
        const val startToken = "&${ParenthesisExpressionNode.startToken}"
        const val endToken = ParenthesisExpressionNode.endToken

        // METHODS ------------------------------------------------------------

        /**
         * Parses an absolute anchor lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode): AbsoluteAnchorLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = AbsoluteAnchorLexemeNode(parser, parent)

            result.isNegated = parser.readText(notOperator)

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            WhitespaceNode.parse(parser)

            while (true) {
                val element = AbsoluteElementAnchorLexemeNode.parse(parser, result) ?: break

                result.elements.add(element)

                WhitespaceNode.parse(parser)
            }

            if (result.elements.isEmpty()) {
                val anchorCursor = parser.reader.saveCursor()

                // To show the end token in the message if it exists.
                parser.readText(NameSelectorNode.groupEndToken)

                throw AngmarParserException(AngmarParserExceptionType.AbsoluteAnchorGroupWithoutAnchors,
                        "Absolute anchor lexemes require at least one anchor.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(anchorCursor.position())
                        message = "Try adding an absolute anchor here"
                    }
                }
            }

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.AbsoluteAnchorGroupWithoutEndToken,
                        "Absolute anchor lexemes require the close parenthesis '$endToken'.") {
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
