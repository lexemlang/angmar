package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.lexemes.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.selectors.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for addition filter lexemes.
 */
internal class AdditionFilterLexemeNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var selector: SelectorNode
    var nextAccess: AccessLexemeNode? = null

    override fun toString() = StringBuilder().apply {
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

        result.add("selector", selector.toTree())
        result.add("nextAccess", nextAccess?.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            AdditionFilterLexemeCompiled.compile(parent, parentSignal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator
        const val startToken = "$+("
        const val endToken = ")"
        const val nextAccessToken = AccessLexemeNode.nextAccessToken

        // METHODS ------------------------------------------------------------

        /**
         * Parses an addition filter lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode): AdditionFilterLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = AdditionFilterLexemeNode(parser, parent)

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            WhitespaceNode.parse(parser)

            result.selector = SelectorNode.parseForAddition(parser, result) ?: let {
                val selectorCursor = parser.reader.saveCursor()

                // To show the end token in the message if it exists.
                parser.readText(NameSelectorNode.groupEndToken)

                throw AngmarParserException(AngmarParserExceptionType.AdditionFilterLexemeWithoutSelector,
                        "Addition lexemes of filters require a selector.") {
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
                throw AngmarParserException(AngmarParserExceptionType.AdditionFilterLexemeWithoutEndToken,
                        "Addition lexemes of filters require the close parenthesis '$endToken'.") {
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

                result.nextAccess = AccessLexemeNode.parse(parser, result) ?: throw AngmarParserException(
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
