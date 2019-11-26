package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.selectors.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for filter lexemes.
 */
internal class FilterLexemeNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var isNegated = false
    lateinit var selector: SelectorNode
    var nextAccess: AccessLexemeNode? = null

    override fun toString() = StringBuilder().apply {
        if (isNegated) {
            append(notOperator)
        }

        append(startToken)
        append(selector)
        append(endToken)

        if (nextAccess != null) {
            append(" $nextAccessToken ")
            append(nextAccess)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isNegated", isNegated)
        result.add("selector", selector.toTree())
        result.add("nextAccess", nextAccess?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            FilterLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator
        const val startToken = "$("
        const val endToken = ")"
        const val nextAccessToken = AccessLexemeNode.nextAccessToken

        // METHODS ------------------------------------------------------------

        /**
         * Parses a filter lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): FilterLexemeNode? {
            parser.fromBuffer(parser.reader.currentPosition(), FilterLexemeNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = FilterLexemeNode(parser, parent, parentSignal)

            result.isNegated = parser.readText(notOperator)

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            WhitespaceNode.parse(parser)

            result.selector = SelectorNode.parse(parser, result, FilterLexemeAnalyzer.signalEndSelector) ?: let {
                val selectorCursor = parser.reader.saveCursor()

                // To show the end token in the message if it exists.
                parser.readText(NameSelectorNode.groupEndToken)

                throw AngmarParserException(AngmarParserExceptionType.FilterLexemeWithoutSelector,
                        "Filter lexemes require a selector.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(selectorCursor.position())
                        message = "Try adding a selector here"
                    }
                }
            }

            WhitespaceNode.parse(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.FilterLexemeWithoutEndToken,
                        "Filter lexemes require the close parenthesis '$endToken'.") {
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

            val preNextAccessCursor = parser.reader.saveCursor()

            WhitespaceNoEOLNode.parse(parser)

            if (parser.readText(nextAccessToken)) {
                WhitespaceNode.parse(parser)

                result.nextAccess = AccessLexemeNode.parse(parser, result, FilterLexemeAnalyzer.signalEndNextAccess)
                        ?: throw AngmarParserException(
                                AngmarParserExceptionType.AdditionFilterLexemeWithoutNextAccessAfterToken,
                                "Addition lexemes require an Access lexeme after the relational token '${AccessLexemeNode.nextAccessToken}'.") {
                            val fullText = parser.reader.readAllText()
                            addSourceCode(fullText, parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(fullText, null) {
                                title = Consts.Logger.hintTitle
                                highlightSection(parser.reader.currentPosition() - 1)
                                message = "Try removing the relational token '$nextAccessToken'"
                            }
                        }
            } else {
                preNextAccessCursor.restore()
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
