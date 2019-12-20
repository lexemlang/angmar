package org.lexem.angmar.parser.descriptive.lexemes.anchors

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Parser for absolute elements of anchor lexemes.
 */
internal class AbsoluteElementAnchorLexemeNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    lateinit var type: AbsoluteAnchorType
    lateinit var expression: ParserNode

    override fun toString() = StringBuilder().apply {
        append(type.identifier)
        append(IndexerNode.startToken)
        append(expression)
        append(IndexerNode.endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("type", type.identifier)
        result.add("expression", expression.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            AbsoluteElementAnchorLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val fromStartIdentifier = "from_start"
        const val fromEndIdentifier = "from_end"
        const val analysisBeginningIdentifier = "analysis_beginning"

        // METHODS ------------------------------------------------------------

        /**
         * Parses an absolute element of an anchor lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): AbsoluteElementAnchorLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = AbsoluteElementAnchorLexemeNode(parser, parent, parentSignal)

            when {
                parser.readText(fromStartIdentifier) -> result.type = AbsoluteAnchorType.FromStart
                parser.readText(fromEndIdentifier) -> result.type = AbsoluteAnchorType.FromEnd
                parser.readText(analysisBeginningIdentifier) -> result.type = AbsoluteAnchorType.AnalysisBeginning
                else -> return null
            }

            val indexer = IndexerNode.parse(parser, result, AbsoluteElementAnchorLexemeAnalyzer.signalEndExpression)
                    ?: throw AngmarParserException(AngmarParserExceptionType.AbsoluteAnchorElementWithoutValue,
                            "An open square bracket '${IndexerNode.startToken}' was expected to init the block of the value of the absolute anchor.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding here the open square bracket '${IndexerNode.startToken}'"
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            message = "Try removing the '${result.type.identifier}' anchor"
                        }
                    }

            result.expression = indexer.expression
            result.expression.parent = result
            result.expression.parentSignal = AbsoluteElementAnchorLexemeAnalyzer.signalEndExpression

            return parser.finalizeNode(result, initCursor)
        }
    }

    // ENUMS ------------------------------------------------------------------

    enum class AbsoluteAnchorType(val identifier: String) {
        FromStart(fromStartIdentifier),
        FromEnd(fromEndIdentifier),
        AnalysisBeginning(analysisBeginningIdentifier)
    }
}
