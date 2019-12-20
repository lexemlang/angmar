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
 * Parser for access lexemes.
 */
internal class AccessLexemeNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var isNegated = false
    var nextAccess: AccessLexemeNode? = null
    lateinit var expression: AccessExpressionLexemeNode

    override fun toString() = StringBuilder().apply {
        if (isNegated) {
            append(notOperator)
        }

        append(expression)

        if (nextAccess != null) {
            append(" $nextAccessToken ")
            append(nextAccess)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isNegated", isNegated)
        result.add("expression", expression.toTree())
        result.add("nextAccess", nextAccess?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            AccessLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator
        const val nextAccessToken = "->"

        // METHODS ------------------------------------------------------------

        /**
         * Parses an access lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): AccessLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = AccessLexemeNode(parser, parent, parentSignal)

            result.isNegated = parser.readText(notOperator)

            val expression = AccessExpressionLexemeNode.parse(parser, result, AccessLexemAnalyzer.signalEndExpression)
            if (expression == null) {
                initCursor.restore()
                return null
            }

            result.expression = expression

            val preNextAccessCursor = parser.reader.saveCursor()

            WhitespaceNoEOLNode.parse(parser)

            if (parser.readText(nextAccessToken)) {
                WhitespaceNode.parse(parser)

                result.nextAccess =
                        parse(parser, result, AccessLexemAnalyzer.signalEndNextAccess) ?: throw AngmarParserException(
                                AngmarParserExceptionType.AccessWithoutNextAccessAfterToken,
                                "Access lexemes require another Access lexeme after the relational token '$nextAccessToken'.") {
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
