package org.lexem.angmar.parser.descriptive.lexemes.anchors

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.selectors.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for relative anchor lexemes.
 */
internal class RelativeAnchorLexemeNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    var isNegated = false
    var elements = mutableListOf<RelativeElementAnchorLexemeNode>()
    lateinit var type: RelativeAnchorType

    override fun toString() = StringBuilder().apply {
        if (isNegated) {
            append(notOperator)
        }

        append(type.token)

        if (elements.isNotEmpty()) {
            append(groupStartToken)
            append(elements.joinToString(" "))
            append(groupEndToken)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isNegated", isNegated)
        result.addProperty("type", type.token)
        result.add("elements", SerializationUtils.listToTest(elements))

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            RelativeAnchorLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator
        const val startTextToken = "^^"
        const val startLineToken = "^"
        const val endTextToken = "$$"
        const val endLineToken = "$"
        const val groupStartToken = ParenthesisExpressionNode.startToken
        const val groupEndToken = ParenthesisExpressionNode.endToken

        // METHODS ------------------------------------------------------------

        /**
         * Parses a relative anchor lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): RelativeAnchorLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = RelativeAnchorLexemeNode(parser, parent, parentSignal)

            result.isNegated = parser.readText(notOperator)

            when {
                parser.readText(startTextToken) -> result.type = RelativeAnchorType.StartText
                parser.readText(endTextToken) -> result.type = RelativeAnchorType.EndText
                else -> {
                    when {
                        parser.readText(startLineToken) -> result.type = RelativeAnchorType.StartLine
                        parser.readText(endLineToken) -> result.type = RelativeAnchorType.EndLine
                        else -> {
                            initCursor.restore()
                            return null
                        }
                    }

                    if (parser.readText(groupStartToken)) {
                        WhitespaceNode.parse(parser)

                        while (true) {
                            val element = RelativeElementAnchorLexemeNode.parse(parser, result,
                                    result.elements.size + RelativeAnchorLexemeAnalyzer.signalEndFirstElement) ?: break

                            result.elements.add(element)

                            WhitespaceNode.parse(parser)
                        }

                        if (result.elements.isEmpty()) {
                            val anchorCursor = parser.reader.saveCursor()

                            // To show the end token in the message if it exists.
                            parser.readText(NameSelectorNode.groupEndToken)

                            throw AngmarParserException(AngmarParserExceptionType.RelativeAnchorGroupWithoutAnchors,
                                    "Relative anchor lexemes require at least one anchor.") {
                                val fullText = parser.reader.readAllText()
                                addSourceCode(fullText, parser.reader.getSource()) {
                                    title = Consts.Logger.codeTitle
                                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                }
                                addSourceCode(fullText, null) {
                                    title = Consts.Logger.hintTitle
                                    highlightCursorAt(anchorCursor.position())
                                    message = "Try adding a relative anchor here"
                                }
                            }
                        }

                        if (!parser.readText(groupEndToken)) {
                            throw AngmarParserException(AngmarParserExceptionType.RelativeAnchorGroupWithoutEndToken,
                                    "Relative anchor lexemes require the close parenthesis '$groupEndToken'.") {
                                val fullText = parser.reader.readAllText()
                                addSourceCode(fullText, parser.reader.getSource()) {
                                    title = Consts.Logger.codeTitle
                                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                }
                                addSourceCode(fullText, null) {
                                    title = Consts.Logger.hintTitle
                                    highlightCursorAt(parser.reader.currentPosition())
                                    message = "Try adding the close parenthesis '$groupEndToken' here"
                                }
                            }
                        }
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }

    // ENUMS ------------------------------------------------------------------

    enum class RelativeAnchorType(val token: String) {
        StartText(startTextToken),
        EndText(endTextToken),
        StartLine(startLineToken),
        EndLine(endLineToken)
    }
}
