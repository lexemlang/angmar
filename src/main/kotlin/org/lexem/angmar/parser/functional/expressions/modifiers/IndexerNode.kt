package org.lexem.angmar.parser.functional.expressions.modifiers

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for indexer i.e. element\[access]
 */
internal class IndexerNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var expression: ParserNode

    override fun toString() = "$startToken$expression$endToken"

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("expression", expression.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = IndexerAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val startToken = "["
        const val endToken = "]"

        // METHODS ------------------------------------------------------------

        /**
         * Parses an indexer expression
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): IndexerNode? {
            parser.fromBuffer(parser.reader.currentPosition(), IndexerNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = IndexerNode(parser, parent, parentSignal)

            if (!parser.readText(startToken)) {
                return null
            }

            WhitespaceNode.parse(parser)

            result.expression = ExpressionsCommons.parseExpression(parser, result, IndexerAnalyzer.signalEndExpression)
                    ?: throw AngmarParserException(AngmarParserExceptionType.IndexerWithoutStartToken,
                            "An expression was expected after the open square bracket '$startToken'.") {
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
                throw AngmarParserException(AngmarParserExceptionType.IndexerWithoutEndToken,
                        "The close square bracket was expected '$endToken'.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the close square bracket '$endToken' here"
                    }
                }

            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
